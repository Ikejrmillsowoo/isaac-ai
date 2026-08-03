package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.message.model.Message;
import org.springframework.stereotype.Service;
import com.isaacai.title.AiTitleGenerator;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.workspace.service.WorkspaceService;


@Service
public class ChatService {

    private final AiChatClient aiChatClient;
    private final ChatSessionService chatSessionService;
    private final AiTitleGenerator aiTitleGenerator;
    private final ConversationService conversationService;
    private final WorkspaceService workspaceService;

private static final String DEFAULT_SYSTEM_PROMPT = """
        You are Isaac AI, a private personal assistant.
        Be clear, practical, and accurate.
        Do not invent facts.
        """;


    public ChatService(
            AiChatClient aiChatClient,
            ChatSessionService chatSessionService,
                AiTitleGenerator aiTitleGenerator,
                ConversationService conversationService,
                WorkspaceService workspaceService                
                

    ) {
        this.aiChatClient = aiChatClient;
        this.chatSessionService = chatSessionService;
        this.aiTitleGenerator = aiTitleGenerator;
        this.conversationService = conversationService;
        this.workspaceService = workspaceService;
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

        String answer =
                aiChatClient.chat(systemPrompt, preparedChat.history());

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

    private String resolveSystemPrompt(String systemPrompt) {
    if (systemPrompt == null || systemPrompt.isBlank()) {
        return DEFAULT_SYSTEM_PROMPT;
    }

    return systemPrompt.trim();
}
}