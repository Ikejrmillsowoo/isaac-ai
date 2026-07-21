package com.isaacai.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatRequest(

        @NotNull(message = "Workspace ID is required.")
        UUID workspaceId,

        @NotNull(message = "Conversation ID is required.")
        UUID conversationId,

        @NotBlank(message = "Message is required.")
        String message

) {
}