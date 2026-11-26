package com.isaacai.chat.service;

import com.isaacai.ai.client.AiStreamingClient;
import com.isaacai.ai.retrieval.KnowledgeContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeRetriever;
import com.isaacai.ai.retrieval.RetrievedKnowledge;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.workspace.service.WorkspaceService;
import com.isaacai.title.AiTitleGenerator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import com.isaacai.ai.memory.MemoryCommandDetector;

import com.isaacai.ai.memory.MemoryContextBuilder;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.service.MemoryService;
import com.isaacai.server.memory.model.MemoryCategory;




import java.util.List;

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
    private final MemoryCommandDetector memoryCommandDetector;
  
  


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
            MemoryCommandDetector memoryCommandDetector
            
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
        this.memoryCommandDetector = memoryCommandDetector;
       
    }

    public Flux<String> stream(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

       String workspaceSystemPrompt =
        workspaceService.findById(
                request.workspaceId()
        ).getSystemPrompt();

        // Retrieve relevant knowledge for this workspace
        List<RetrievedKnowledge> retrievedKnowledge =
                knowledgeRetriever.retrieve(
                        request.workspaceId(),
                        request.message()
                );

        // Convert retrieved chunks into context
        String knowledgeContext =
                knowledgeContextBuilder.build(
                        retrievedKnowledge
                );


        if (memoryCommandDetector.isRememberCommand(
        request.message()
)) {

    String memoryContent =
            memoryCommandDetector.extractRememberContent(
                    request.message()
            );

    memoryService.create(
            request.workspaceId(),
            memoryContent,
            MemoryCategory.CONTEXT,
            request.conversationId()
    );
}

if (memoryCommandDetector.isForgetCommand(
        request.message()
)) {

    String memoryContent =
            memoryCommandDetector.extractForgetContent(
                    request.message()
            );

    memoryService.forget(
            request.workspaceId(),
            memoryContent
    );
}
        // Retrieve memory context
        List<Memory> memories =
        memoryService.findAll(
                request.workspaceId()
        );

String memoryContext =
        memoryContextBuilder.build(
                memories
        );

        // Combine the workspace prompt and knowledge
       String augmentedSystemPrompt =
        chatPromptBuilder.build(
                workspaceSystemPrompt,
                knowledgeContext,
                memoryContext
        );

        StringBuilder assistantResponse =
                new StringBuilder();

        return aiStreamingClient
                .stream(
                        augmentedSystemPrompt,
                        preparedChat.history()
                )
                .doOnNext(assistantResponse::append)
                .doOnComplete(() -> {

                    String answer =
                            assistantResponse.toString();

                    if (answer.isBlank()) {
                        return;
                    }

                    chatSessionService
                            .saveAssistantMessage(
                                    request,
                                    answer
                            );

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
                });
    }


}