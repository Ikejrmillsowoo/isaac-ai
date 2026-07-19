package com.isaacai.server.conversation.model;

import com.isaacai.server.workspace.model.Workspace;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {

    private static final int MAX_TITLE_LENGTH = 200;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "workspace_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_conversations_workspace"
            )
    )
    private Workspace workspace;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false)
    private boolean archived;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected Conversation() {
        // Required by JPA.
    }

    public Conversation(
            Workspace workspace,
            String title
    ) {
        this.id = UUID.randomUUID();
        this.workspace = requireWorkspace(workspace);
        this.title = normalizeTitle(title);
        this.pinned = false;
        this.archived = false;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void rename(String title) {
        this.title = normalizeTitle(title);
    }

    public void pin() {
        this.pinned = true;
    }

    public void unpin() {
        this.pinned = false;
    }

    public void archive() {
        this.archived = true;
    }

    public void restore() {
        this.archived = false;
    }

    public UUID getId() {
        return id;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isArchived() {
        return archived;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static Workspace requireWorkspace(Workspace workspace) {
        if (workspace == null) {
            throw new IllegalArgumentException(
                    "Conversation workspace is required."
            );
        }

        return workspace;
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Conversation title is required."
            );
        }

        String normalizedTitle = title.trim();

        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Conversation title must not exceed "
                            + MAX_TITLE_LENGTH
                            + " characters."
            );
        }

        return normalizedTitle;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Conversation conversation)) {
            return false;
        }

        return id != null && id.equals(conversation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}