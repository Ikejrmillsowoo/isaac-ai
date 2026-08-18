package com.isaacai.server.knowledge.repository;

import com.isaacai.server.knowledge.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeDocumentRepository
        extends JpaRepository<KnowledgeDocument, UUID> {

    List<KnowledgeDocument>
    findAllByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);
}