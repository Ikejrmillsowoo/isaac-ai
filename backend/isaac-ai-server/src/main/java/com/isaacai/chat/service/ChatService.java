
package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.ai.memory.MemoryCommandHandler;
import com.isaacai.ai.memory.MemoryContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeRetriever;
import com.isaacai.ai.retrieval.RetrievedKnowledge;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.service.MemoryService;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.workspace.service.WorkspaceService;
import com.isaacai.title.AiTitleGenerator;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final AiChatClient aiChatClient;
    private final ChatSessionService chatSessionService;
    private final AiTitleGenerator aiTitleGenerator;
    private final ConversationService conversationService;
    private final WorkspaceService workspaceService;
    private final KnowledgeContextBuilder knowledgeContextBuilder;
    private final KnowledgeRetriever knowledgeRetriever;
    private final ChatPromptBuilder chatPromptBuilder;
    private final MemoryContextBuilder memoryContextBuilder;
    private final MemoryService memoryService;
    private final MemoryCommandHandler memoryCommandHandler;

    public ChatService(
            AiChatClient aiChatClient,
            ChatSessionService chatSessionService,
            AiTitleGenerator aiTitleGenerator,
            ConversationService conversationService,
            WorkspaceService workspaceService,
            KnowledgeContextBuilder knowledgeContextBuilder,
            KnowledgeRetriever knowledgeRetriever,
            ChatPromptBuilder chatPromptBuilder,
            MemoryContextBuilder memoryContextBuilder,
            MemoryService memoryService,
            MemoryCommandHandler memoryCommandHandler
    ) {
        this.aiChatClient = aiChatClient;
        this.chatSessionService = chatSessionService;
        this.aiTitleGenerator = aiTitleGenerator;
        this.conversationService = conversationService;
        this.workspaceService = workspaceService;
        this.knowledgeContextBuilder = knowledgeContextBuilder;
        this.knowledgeRetriever = knowledgeRetriever;
        this.chatPromptBuilder = chatPromptBuilder;
        this.memoryContextBuilder = memoryContextBuilder;
        this.memoryService = memoryService;
        this.memoryCommandHandler = memoryCommandHandler;
    }

    public ChatResponse chat(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

        Optional<String> memoryResponse =
                memoryCommandHandler.handle(request);

        String answer;

        if (memoryResponse.isPresent()) {

            answer = memoryResponse.get();

        } else {

            answer = generateAiResponse(
                    request,
                    preparedChat
            );
        }

        Message assistantMessage =
                chatSessionService.saveAssistantMessage(
                        request,
                        answer
                );

        if (memoryResponse.isEmpty() && !answer.isBlank()) {
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

        return new ChatResponse(
                request.conversationId(),
                preparedChat.userMessage().getId(),
                assistantMessage.getId(),
                answer
        );
    }

    private String generateAiResponse(
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
                memoryContextBuilder.build(memories);

        if (memoryContext == null) {
            memoryContext = "";
        }

        String augmentedSystemPrompt =
                chatPromptBuilder.build(
                        workspaceSystemPrompt,
                        knowledgeContext,
                        memoryContext
                );

        return aiChatClient.chat(
                augmentedSystemPrompt,
                preparedChat.history()
        );
    }
}
