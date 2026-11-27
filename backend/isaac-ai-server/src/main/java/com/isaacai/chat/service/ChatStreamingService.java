
package com.isaacai.chat.service;

import com.isaacai.ai.client.AiStreamingClient;
import com.isaacai.ai.memory.MemoryCommandHandler;
import com.isaacai.ai.memory.MemoryContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeRetriever;
import com.isaacai.ai.retrieval.RetrievedKnowledge;
import com.isaacai.ai.retrieval.KnowledgeSources;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.service.MemoryService;
import com.isaacai.server.workspace.service.WorkspaceService;
import com.isaacai.title.AiTitleGenerator;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Service
public class ChatStreamingService {

    private final AiStreamingClient aiStreamingClient;
    private final ChatSessionService chatSessionService;
    private final AiTitleGenerator aiTitleGenerator;
    private final ConversationService conversationService;
    private final WorkspaceService workspaceService;
    private final KnowledgeRetriever knowledgeRetriever;
    private final KnowledgeContextBuilder knowledgeContextBuilder;
    private final ChatPromptBuilder chatPromptBuilder;
    private final MemoryService memoryService;
    private final MemoryContextBuilder memoryContextBuilder;
    private final MemoryCommandHandler memoryCommandHandler;

    public ChatStreamingService(
            AiStreamingClient aiStreamingClient,
            ChatSessionService chatSessionService,
            AiTitleGenerator aiTitleGenerator,
            ConversationService conversationService,
            WorkspaceService workspaceService,
            KnowledgeRetriever knowledgeRetriever,
            KnowledgeContextBuilder knowledgeContextBuilder,
            ChatPromptBuilder chatPromptBuilder,
            MemoryService memoryService,
            MemoryContextBuilder memoryContextBuilder,
            MemoryCommandHandler memoryCommandHandler
    ) {
        this.aiStreamingClient = aiStreamingClient;
        this.chatSessionService = chatSessionService;
        this.aiTitleGenerator = aiTitleGenerator;
        this.conversationService = conversationService;
        this.workspaceService = workspaceService;
        this.knowledgeRetriever = knowledgeRetriever;
        this.knowledgeContextBuilder = knowledgeContextBuilder;
        this.chatPromptBuilder = chatPromptBuilder;
        this.memoryService = memoryService;
        this.memoryContextBuilder = memoryContextBuilder;
        this.memoryCommandHandler = memoryCommandHandler;
    }

    public Flux<String> stream(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

        Optional<String> memoryResponse =
                memoryCommandHandler.handle(request);

        Flux<String> responseStream;

        if (memoryResponse.isPresent()) {

            responseStream = Flux.just(memoryResponse.get());

        } else {

            responseStream = generateAiStream(
                    request,
                    preparedChat
            );
        }

        StringBuilder assistantResponse =
                new StringBuilder();

        return responseStream
                .doOnNext(assistantResponse::append)
                .doOnComplete(() ->
                        completeConversation(
                                request,
                                assistantResponse.toString(),
                                memoryResponse.isEmpty()
                        )
                );
    }

    private Flux<String> generateAiStream(
            ChatRequest request,
            PreparedChat preparedChat
    ) {

        String workspaceSystemPrompt =
                workspaceService.findById(
                        request.workspaceId()
                ).getSystemPrompt();

        List<RetrievedKnowledge> retrievedKnowledge =
                knowledgeRetriever.retrieve(
                        request.workspaceId(),
                        request.message()
                );

        String knowledgeContext =
                knowledgeContextBuilder.build(
                        retrievedKnowledge
                );

        List<Memory> memories =
                memoryService.findAll(
                        request.workspaceId()
                );

        String memoryContext =
                memoryContextBuilder.build(memories, request.message());

        if (memoryContext == null) {
            memoryContext = "";
        }

        String augmentedSystemPrompt =
                chatPromptBuilder.build(
                        workspaceSystemPrompt,
                        knowledgeContext,
                        memoryContext
                );

        String footer = KnowledgeSources.footer(retrievedKnowledge);
        Flux<String> stream = aiStreamingClient.streamWithTools(
                augmentedSystemPrompt,
                preparedChat.history(), request.workspaceId()
        );
        if (footer.isEmpty()) return stream;
        return Flux.defer(() -> {
            java.util.concurrent.atomic.AtomicBoolean hasAnswer = new java.util.concurrent.atomic.AtomicBoolean();
            return stream.doOnNext(chunk -> { if (!chunk.isBlank()) hasAnswer.set(true); })
                    .concatWith(Flux.defer(() -> hasAnswer.get() ? Flux.just(footer) : Flux.empty()));
        });
    }

    private void completeConversation(
            ChatRequest request,
            String answer,
            boolean ordinaryChat
    ) {

        if (answer.isBlank()) {
            return;
        }

        chatSessionService.saveAssistantMessage(
                request,
                answer
        );

        if (ordinaryChat) {
            memoryCommandHandler.captureAutomaticMemory(request);
        }

        Conversation conversation =
                conversationService.findById(
                        request.workspaceId(),
                        request.conversationId()
                );

        if (conversation.hasDefaultTitle()) {

            String generatedTitle =
                    aiTitleGenerator.generateTitle(
                            request.message()
                    );

            conversationService.rename(
                    request.workspaceId(),
                    request.conversationId(),
                    generatedTitle
            );
        }
    }
}
