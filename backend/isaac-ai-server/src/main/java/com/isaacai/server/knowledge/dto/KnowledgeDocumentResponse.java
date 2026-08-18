package com.isaacai.server.knowledge.dto;

import com.isaacai.server.knowledge.model.KnowledgeDocument;
import com.isaacai.server.knowledge.model.KnowledgeDocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeDocumentResponse(
        UUID id,
        UUID workspaceId,
        String name,
        String originalFilename,
        String contentType,
        KnowledgeDocumentStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static KnowledgeDocumentResponse from(
            KnowledgeDocument document
    ) {
        return new KnowledgeDocumentResponse(
                document.getId(),
                document.getWorkspace().getId(),
                document.getName(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}