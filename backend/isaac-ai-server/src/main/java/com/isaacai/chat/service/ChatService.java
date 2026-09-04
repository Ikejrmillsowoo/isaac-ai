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

import java.util.List;

@Service
public class ChatService {

    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are Isaac AI, a private personal assistant.
            Be clear, practical, and accurate.
            Do not invent facts.
            """;

    private final AiChatClient aiChatClient;
    private final ChatSessionService chatSessionService;
    private final AiTitleGenerator aiTitleGenerator;
    private final ConversationService conversationService;
    private final WorkspaceService workspaceService;
    private final KnowledgeContextBuilder knowledgeContextBuilder;
    private final KnowledgeRetriever knowledgeRetriever;

    public ChatService(
            AiChatClient aiChatClient,
            ChatSessionService chatSessionService,
            AiTitleGenerator aiTitleGenerator,
            ConversationService conversationService,
            WorkspaceService workspaceService,
            KnowledgeContextBuilder knowledgeContextBuilder,
            KnowledgeRetriever knowledgeRetriever
    ) {
        this.aiChatClient = aiChatClient;
        this.chatSessionService = chatSessionService;
        this.aiTitleGenerator = aiTitleGenerator;
        this.conversationService = conversationService;
        this.workspaceService = workspaceService;
        this.knowledgeContextBuilder = knowledgeContextBuilder;
        this.knowledgeRetriever = knowledgeRetriever;
    }

    public ChatResponse chat(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

        String systemPrompt =
                resolveSystemPrompt(
                        workspaceService.findById(
                                request.workspaceId()
                        ).getSystemPrompt()
                );

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

        // Combine workspace prompt with retrieved knowledge
        String augmentedSystemPrompt =
                buildSystemPrompt(
                        systemPrompt,
                        knowledgeContext
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

    private String resolveSystemPrompt(
            String systemPrompt
    ) {

        if (systemPrompt == null ||
                systemPrompt.isBlank()) {
            return DEFAULT_SYSTEM_PROMPT;
        }

        return systemPrompt.trim();
    }

    private String buildSystemPrompt(
            String systemPrompt,
            String knowledgeContext
    ) {

        if (knowledgeContext == null ||
                knowledgeContext.isBlank()) {
            return systemPrompt;
        }

        return """
                %s

                You have access to relevant knowledge retrieved
                from documents in this workspace.

                Use the workspace knowledge below when it is
                relevant to the user's question.

                Do not claim that the knowledge says something
                that is not supported by the provided text.

                If the provided knowledge does not contain enough
                information to answer the question, say so rather
                than inventing information.

                --- WORKSPACE KNOWLEDGE ---

                %s

                --- END WORKSPACE KNOWLEDGE ---
                """.formatted(
                systemPrompt,
                knowledgeContext
        );
    }
}