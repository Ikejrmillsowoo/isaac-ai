package com.isaacai.server.knowledge.repository;

import com.isaacai.server.knowledge.model.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeChunkRepository
        extends JpaRepository<KnowledgeChunk, UUID> {

    List<KnowledgeChunk>
    findAllByDocumentIdOrderByChunkIndexAsc(
            UUID documentId
    );

    long countByDocumentId(UUID documentId);

    void deleteAllByDocumentId(UUID documentId);
}