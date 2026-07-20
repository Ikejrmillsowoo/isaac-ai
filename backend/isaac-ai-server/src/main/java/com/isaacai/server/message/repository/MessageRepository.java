package com.isaacai.server.message.repository;

import com.isaacai.server.message.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository
        extends JpaRepository<Message, UUID> {

    List<Message> findAllByConversationIdOrderByCreatedAtAsc(
            UUID conversationId
    );

    Optional<Message> findByIdAndConversationId(
            UUID messageId,
            UUID conversationId
    );

    long countByConversationId(UUID conversationId);
}