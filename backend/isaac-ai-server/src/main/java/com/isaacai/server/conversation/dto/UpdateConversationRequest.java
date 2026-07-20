package com.isaacai.server.conversation.dto;

import jakarta.validation.constraints.Size;

public record UpdateConversationRequest(

        @Size(
                max = 200,
                message = "Conversation title must not exceed 200 characters."
        )
        String title
) {
}