package com.isaacai.server.workspace.dto;

import java.time.Instant;
import java.util.UUID;

import com.isaacai.server.workspace.model.Workspace;

public record WorkspaceResponse(
        UUID id,
        String name,
        String description,
        String systemPrompt,
        String color,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {

    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getSystemPrompt(),
                workspace.getColor(),
                workspace.isArchived(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }
}