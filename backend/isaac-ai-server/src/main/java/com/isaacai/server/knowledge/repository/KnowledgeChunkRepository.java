package com.isaacai.server.knowledge.repository;

import com.isaacai.server.knowledge.model.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying
@Transactional
@Query(
    value = """
        UPDATE knowledge_chunks
        SET embedding = CAST(:embedding AS vector)
        WHERE id = :chunkId
        """,
    nativeQuery = true
)
void updateEmbedding(
        @Param("chunkId") UUID chunkId,
        @Param("embedding") String embedding
);
}