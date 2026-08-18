package com.isaacai.server.knowledge.service;

import com.isaacai.server.knowledge.model.KnowledgeDocument;
import com.isaacai.server.knowledge.repository.KnowledgeDocumentRepository;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class KnowledgeDocumentService {

    private final KnowledgeDocumentRepository repository;
    private final WorkspaceService workspaceService;


    public KnowledgeDocumentService(
            KnowledgeDocumentRepository repository,
            WorkspaceService workspaceService
    ) {
        this.repository = repository;
        this.workspaceService = workspaceService;
    }

    public KnowledgeDocument create(
            UUID workspaceId,
            String name,
            String originalFilename,
            String contentType
    ) {
        Workspace workspace =
                workspaceService.findById(workspaceId);

        KnowledgeDocument document =
                new KnowledgeDocument(
                        workspace,
                        name,
                        originalFilename,
                        contentType
                );

        return repository.save(document);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeDocument> findAll(
            UUID workspaceId
    ) {
        workspaceService.findById(workspaceId);

        return repository
                .findAllByWorkspaceIdOrderByCreatedAtDesc(
                        workspaceId
                );
    }

    public KnowledgeDocument upload(
        UUID workspaceId,
        MultipartFile file
) {
    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException(
                "Knowledge document file is required."
        );
    }

    Workspace workspace =
            workspaceService.findById(workspaceId);

    String originalFilename =
            file.getOriginalFilename();

    String name =
            originalFilename != null
                    ? originalFilename
                    : "Untitled Document";

    KnowledgeDocument document =
            new KnowledgeDocument(
                    workspace,
                    name,
                    originalFilename,
                    file.getContentType()
            );

    return repository.save(document);
}


}