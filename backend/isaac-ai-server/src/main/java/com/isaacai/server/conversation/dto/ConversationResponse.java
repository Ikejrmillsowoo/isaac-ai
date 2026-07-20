package com.isaacai.server.conversation.dto;

import com.isaacai.server.conversation.model.Conversation;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        UUID workspaceId,
        String title,
        boolean pinned,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {

    public static ConversationResponse from(
            Conversation conversation
    ) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getWorkspace().getId(),
                conversation.getTitle(),
                conversation.isPinned(),
                conversation.isArchived(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}