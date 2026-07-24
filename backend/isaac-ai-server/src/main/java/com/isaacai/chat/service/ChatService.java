package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.message.model.Message;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final AiChatClient aiChatClient;
    private final ChatSessionService chatSessionService;

    public ChatService(
            AiChatClient aiChatClient,
            ChatSessionService chatSessionService
    ) {
        this.aiChatClient = aiChatClient;
        this.chatSessionService = chatSessionService;
    }

    public ChatResponse chat(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

        String answer =
                aiChatClient.chat(preparedChat.history());

        Message assistantMessage =
                chatSessionService.saveAssistantMessage(
                        request,
                        answer
                );

        return new ChatResponse(
                request.conversationId(),
                preparedChat.userMessage().getId(),
                assistantMessage.getId(),
                answer
        );
    }
}