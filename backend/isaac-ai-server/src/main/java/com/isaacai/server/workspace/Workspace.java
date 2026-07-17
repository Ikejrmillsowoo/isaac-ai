package com.isaacai.server.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "workspaces")
public class Workspace {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "archived", nullable = false)
    private boolean archived;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Workspace() {
        // Required by JPA
    }

    public Workspace(
            String name,
            String description,
            String systemPrompt,
            String color
    ) {
        this.id = UUID.randomUUID();
        this.name = normalizeRequiredName(name);
        this.description = normalizeOptionalText(description);
        this.systemPrompt = normalizeOptionalText(systemPrompt);
        this.color = normalizeOptionalText(color);
        this.archived = false;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void rename(String name) {
        this.name = normalizeRequiredName(name);
    }

    public void updateDescription(String description) {
        this.description = normalizeOptionalText(description);
    }

    public void updateSystemPrompt(String systemPrompt) {
        this.systemPrompt = normalizeOptionalText(systemPrompt);
    }

    public void updateColor(String color) {
        this.color = normalizeOptionalText(color);
    }

    public void archive() {
        this.archived = true;
    }

    public void restore() {
        this.archived = false;
    }

    private static String normalizeRequiredName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Workspace name must not be blank");
        }

        String normalized = value.trim();

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    "Workspace name must not exceed 100 characters"
            );
        }

        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty() ? null : normalized;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getColor() {
        return color;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Workspace workspace)) {
            return false;
        }

        return id != null && id.equals(workspace.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}