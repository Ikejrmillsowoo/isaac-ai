package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final AiChatClient aiChatClient;
    private final MessageService messageService;

    public ChatService(
            AiChatClient aiChatClient,
            MessageService messageService
    ) {
        this.aiChatClient = aiChatClient;
        this.messageService = messageService;
    }

    public ChatResponse chat(ChatRequest request) {

        Message userMessage =
                messageService.createUserMessage(
                        request.workspaceId(),
                        request.conversationId(),
                        request.message()
                );

        List<Message> history =
                messageService.findConversationMessages(
                        request.workspaceId(),
                        request.conversationId()
                );

        history.forEach(m ->
        System.out.println(
                m.getRole() + " -> " + m.getContent()
        )
);

        String answer = aiChatClient.chat(history);

        Message assistantMessage =
                messageService.createAssistantMessage(
                        request.workspaceId(),
                        request.conversationId(),
                        answer
                );

        return new ChatResponse(
                request.conversationId(),
                userMessage.getId(),
                assistantMessage.getId(),
                answer
        );
    }
}