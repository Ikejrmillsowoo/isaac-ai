package com.isaacai.server.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMessageRequest(

        @NotBlank(message = "Message content is required.")
        @Size(
                max = 20_000,
                message = "Message content must not exceed 20,000 characters."
        )
        String content

) {
}