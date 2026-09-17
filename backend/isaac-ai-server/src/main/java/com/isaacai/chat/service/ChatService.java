package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.ai.retrieval.KnowledgeContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeRetriever;
import com.isaacai.ai.retrieval.RetrievedKnowledge;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.workspace.service.WorkspaceService;
import com.isaacai.title.AiTitleGenerator;
import org.springframework.stereotype.Service;

import com.isaacai.ai.memory.MemoryContextBuilder;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.service.MemoryService;

import com.isaacai.ai.memory.MemoryCommandDetector;
import com.isaacai.server.memory.model.MemoryCategory;

import java.util.List;

@Service
public class ChatService {

//     private static final String DEFAULT_SYSTEM_PROMPT = """
//             You are Isaac AI, a private personal assistant.
//             Be clear, practical, and accurate.
//             Do not invent facts.
//             """;

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
    private final MemoryCommandDetector memoryCommandDetector;

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
            MemoryCommandDetector memoryCommandDetector

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
        this.memoryCommandDetector = memoryCommandDetector;
        

    }

    public ChatResponse chat(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

       String workspaceSystemPrompt =
        workspaceService.findById(
                request.workspaceId()
        ).getSystemPrompt();

        // Retrieve relevant workspace knowledge
        List<RetrievedKnowledge> retrievedKnowledge =
                knowledgeRetriever.retrieve(
                        request.workspaceId(),
                        request.message()
                );

        // Convert retrieved chunks into AI context
        String knowledgeContext =
                knowledgeContextBuilder.build(
                        retrievedKnowledge
                );

        // Build memory context
        List<Memory> memories =
                memoryService.findAll(
                        request.workspaceId()
                );

        String memoryContext =
                memoryContextBuilder.build(
                        memories
                );

        if (memoryContext == null) {
    memoryContext = "";
}

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

        // Combine workspace prompt with retrieved knowledge
        String augmentedSystemPrompt =
        chatPromptBuilder.build(
                workspaceSystemPrompt,
                knowledgeContext,
                memoryContext
        );

        String answer =
                aiChatClient.chat(
                        augmentedSystemPrompt,
                        preparedChat.history()
                );

        Message assistantMessage =
                chatSessionService.saveAssistantMessage(
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

        return new ChatResponse(
                request.conversationId(),
                preparedChat.userMessage().getId(),
                assistantMessage.getId(),
                answer
        );
    }

}