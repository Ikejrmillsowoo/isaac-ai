package com.isaacai.server.conversation.repository;

import com.isaacai.server.conversation.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository
        extends JpaRepository<Conversation, UUID> {

    List<Conversation>
    findAllByWorkspaceIdAndArchivedFalseOrderByPinnedDescUpdatedAtDesc(
            UUID workspaceId
    );

    List<Conversation>
    findAllByWorkspaceIdAndArchivedTrueOrderByUpdatedAtDesc(
            UUID workspaceId
    );

    Optional<Conversation> findByIdAndWorkspaceId(
            UUID conversationId,
            UUID workspaceId
    );
}