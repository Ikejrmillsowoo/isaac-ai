package com.isaacai.chat.service;

import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.service.MessageService;
import org.springframework.stereotype.Service;
import com.isaacai.server.workspace.service.WorkspaceService;
import com.isaacai.server.conversation.service.ConversationService;

import java.util.List;

@Service
public class ChatSessionService {

    
private final MessageService messageService;

    public ChatSessionService(MessageService messageService) {
        this.messageService = messageService;
     
    }

    public PreparedChat prepareConversation(ChatRequest request) {

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

        return new PreparedChat(
                userMessage,
                history
        );
    }

    public Message saveAssistantMessage(
            ChatRequest request,
            String answer
    ) {
        return messageService.createAssistantMessage(
                request.workspaceId(),
                request.conversationId(),
                answer
        );
    }
}