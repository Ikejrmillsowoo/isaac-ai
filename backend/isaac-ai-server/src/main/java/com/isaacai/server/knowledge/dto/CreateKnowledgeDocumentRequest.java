package com.isaacai.server.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateKnowledgeDocumentRequest(

        @NotBlank
        String name,

        String originalFilename,

        String contentType

) {
}