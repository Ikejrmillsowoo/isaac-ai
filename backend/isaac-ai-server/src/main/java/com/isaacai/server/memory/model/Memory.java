package com.isaacai.server.memory.model;

import com.isaacai.server.workspace.model.Workspace;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "memories")
public class Memory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "workspace_id",
            nullable = false
    )
    private Workspace workspace;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryCategory category;

    @Column(name = "source_conversation_id")
    private UUID sourceConversationId;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected Memory() {
    }

    public Memory(
            Workspace workspace,
            String content,
            MemoryCategory category,
            UUID sourceConversationId
    ) {
        this.workspace = workspace;
        this.content = content;
        this.category = category;
        this.sourceConversationId =
                sourceConversationId;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public String getContent() {
        return content;
    }

    public MemoryCategory getCategory() {
        return category;
    }

    public UUID getSourceConversationId() {
        return sourceConversationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateCategory(
            MemoryCategory category
    ) {
        this.category = category;
    }

public void setContent(String content) {
    this.content = content;
}

public void setCategory(MemoryCategory category) {
    this.category = category;
}


}