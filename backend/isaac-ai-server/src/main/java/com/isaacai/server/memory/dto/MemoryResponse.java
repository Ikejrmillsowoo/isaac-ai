package com.isaacai.server.memory.dto;

import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.model.MemoryCategory;

import java.time.Instant;
import java.util.UUID;

public record MemoryResponse(
        UUID id,
        String content,
        MemoryCategory category,
        UUID sourceConversationId,
        Instant createdAt,
        Instant updatedAt
) {

    public static MemoryResponse from(
            Memory memory
    ) {
        return new MemoryResponse(
                memory.getId(),
                memory.getContent(),
                memory.getCategory(),
                memory.getSourceConversationId(),
                memory.getCreatedAt(),
                memory.getUpdatedAt()
        );
    }
}