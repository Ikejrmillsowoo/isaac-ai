package com.isaacai.server.message.service;

import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.message.exception.MessageNotFoundException;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import com.isaacai.server.message.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;

    public MessageService(
            MessageRepository messageRepository,
            ConversationService conversationService
    ) {
        this.messageRepository = messageRepository;
        this.conversationService = conversationService;
    }

    public Message create(
            UUID workspaceId,
            UUID conversationId,
            MessageRole role,
            String content
    ) {
        Conversation conversation =
                conversationService.findById(
                        workspaceId,
                        conversationId
                );

        Message message = new Message(
                conversation,
                role,
                content
        );

        return messageRepository.save(message);
    }

    public Message createUserMessage(
        UUID workspaceId,
        UUID conversationId,
        String content
) {
    return create(
            workspaceId,
            conversationId,
            MessageRole.USER,
            content
    );
}

    @Transactional(readOnly = true)
    public Message findById(
            UUID workspaceId,
            UUID conversationId,
            UUID messageId
    ) {
        conversationService.findById(
                workspaceId,
                conversationId
        );

        return messageRepository
                .findByIdAndConversationId(
                        messageId,
                        conversationId
                )
                .orElseThrow(
                        () -> new MessageNotFoundException(messageId)
                );
    }

    @Transactional(readOnly = true)
    public List<Message> findConversationMessages(
            UUID workspaceId,
            UUID conversationId
    ) {
        conversationService.findById(
                workspaceId,
                conversationId
        );

        return messageRepository
                .findAllByConversationIdOrderByCreatedAtAscIdAsc(
                        conversationId
                );
    }

    public Message update(
            UUID workspaceId,
            UUID conversationId,
            UUID messageId,
            String content
    ) {
        Message message = findById(
                workspaceId,
                conversationId,
                messageId
        );

        if (content != null) {
            message.updateContent(content);
        }

        return message;
    }

    public void delete(
            UUID workspaceId,
            UUID conversationId,
            UUID messageId
    ) {
        Message message = findById(
                workspaceId,
                conversationId,
                messageId
        );

        messageRepository.delete(message);
    }
}