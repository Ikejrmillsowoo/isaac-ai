package com.isaacai.server.workspace.dto;

import jakarta.validation.constraints.Size;

public record UpdateWorkspaceRequest(

        @Size(
                max = 100,
                message = "Workspace name must not exceed 100 characters"
        )
        String name,

        @Size(
                max = 1000,
                message = "Description must not exceed 1000 characters"
        )
        String description,

        @Size(
                max = 10000,
                message = "System prompt must not exceed 10000 characters"
        )
        String systemPrompt,

        @Size(
                max = 20,
                message = "Color must not exceed 20 characters"
        )
        String color
) {
}