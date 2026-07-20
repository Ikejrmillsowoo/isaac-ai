package com.isaacai.server.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateConversationRequest(

        @NotBlank(message = "Conversation title is required.")
        @Size(
                max = 200,
                message = "Conversation title must not exceed 255 characters."
        )
        String title

) {
}