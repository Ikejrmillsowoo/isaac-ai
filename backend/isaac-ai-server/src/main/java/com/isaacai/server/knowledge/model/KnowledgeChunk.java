package com.isaacai.server.knowledge.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_chunks")
public class KnowledgeChunk {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_knowledge_chunks_document"
            )
    )
    private KnowledgeDocument document;

    @Column(
            name = "chunk_index",
            nullable = false
    )
    private int chunkIndex;

    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected KnowledgeChunk() {
    }

    public KnowledgeChunk(
            KnowledgeDocument document,
            int chunkIndex,
            String content
    ) {
        this.id = UUID.randomUUID();
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.content = content;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public KnowledgeDocument getDocument() {
        return document;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}