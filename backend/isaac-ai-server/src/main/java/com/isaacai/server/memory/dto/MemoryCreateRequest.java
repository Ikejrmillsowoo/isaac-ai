package com.isaacai.server.memory.dto;

import com.isaacai.server.memory.model.MemoryCategory;

public record MemoryCreateRequest(
        String content,
        MemoryCategory category
) {
}