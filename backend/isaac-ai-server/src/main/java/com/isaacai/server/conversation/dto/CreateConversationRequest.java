package com.isaacai.server.conversation.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateConversationRequest(

        @NotBlank(message = "Conversation title is required.")
        @Size(
                max = 200,
                message = "Conversation title must not exceed 200 characters."
        )
        String title
) {
}