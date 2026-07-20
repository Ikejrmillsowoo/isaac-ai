package com.isaacai.server.message.model;

import com.isaacai.server.conversation.model.Conversation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "messages")
public class Message {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_messages_conversation"
            )
    )
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

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

    protected Message() {
        // Required by JPA.
    }

    public Message(
            Conversation conversation,
            MessageRole role,
            String content
    ) {
        this.id = UUID.randomUUID();
        this.conversation = requireConversation(conversation);
        this.role = requireRole(role);
        this.content = normalizeContent(content);
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

    public void updateContent(String content) {
        this.content = normalizeContent(content);
    }

    public UUID getId() {
        return id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static Conversation requireConversation(
            Conversation conversation
    ) {
        if (conversation == null) {
            throw new IllegalArgumentException(
                    "Message conversation is required."
            );
        }

        return conversation;
    }

    private static MessageRole requireRole(MessageRole role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "Message role is required."
            );
        }

        return role;
    }

    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Message content is required."
            );
        }

        return content.trim();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Message message)) {
            return false;
        }

        return id != null && id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}