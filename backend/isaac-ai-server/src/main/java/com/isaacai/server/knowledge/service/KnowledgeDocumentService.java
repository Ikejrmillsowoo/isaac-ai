package com.isaacai.server.knowledge.service;

import com.isaacai.server.knowledge.model.KnowledgeDocument;
import com.isaacai.server.knowledge.repository.KnowledgeDocumentRepository;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.isaacai.server.knowledge.extraction.DocumentTextExtractor;

import com.isaacai.server.knowledge.model.KnowledgeChunk;
import com.isaacai.server.knowledge.repository.KnowledgeChunkRepository;

import com.isaacai.ai.embedding.EmbeddingClient;
import com.isaacai.ai.embedding.VectorFormatter;



import java.util.ArrayList;


import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class KnowledgeDocumentService {

    private final KnowledgeDocumentRepository repository;
    private final WorkspaceService workspaceService;
    private final DocumentTextExtractor textExtractor;
    private final DocumentChunkingService chunkingService;
    private final KnowledgeChunkRepository chunkRepository;
    private final EmbeddingClient embeddingClient;
    private final VectorFormatter vectorFormatter;


    public KnowledgeDocumentService(
            KnowledgeDocumentRepository repository,
            WorkspaceService workspaceService,
            DocumentTextExtractor textExtractor,
            DocumentChunkingService chunkingService,
            KnowledgeChunkRepository chunkRepository,
            EmbeddingClient embeddingClient,
            VectorFormatter vectorFormatter
    ) {
        this.repository = repository;
        this.workspaceService = workspaceService;
        this.textExtractor = textExtractor;
        this.chunkingService = chunkingService;
        this.chunkRepository = chunkRepository;
        this.embeddingClient = embeddingClient;
        this.vectorFormatter = vectorFormatter;
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

    String extractedText =
            textExtractor.extract(file);

    if (extractedText == null || extractedText.isBlank()) {
        throw new IllegalArgumentException(
                "No readable text could be extracted from the document."
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

    KnowledgeDocument savedDocument =
            repository.save(document);

    List<String> chunks =
            chunkingService.chunk(extractedText);

    List<KnowledgeChunk> knowledgeChunks =
            new ArrayList<>();

    for (int index = 0; index < chunks.size(); index++) {

        KnowledgeChunk knowledgeChunk =
                new KnowledgeChunk(
                        savedDocument,
                        index,
                        chunks.get(index)
                );

        knowledgeChunks.add(knowledgeChunk);
    }

    List<KnowledgeChunk> savedChunks =
        chunkRepository.saveAll(knowledgeChunks);

for (KnowledgeChunk chunk : savedChunks) {

    float[] embedding =
            embeddingClient.embed(
                    chunk.getContent()
            );

    if (embedding.length != 768) {
        throw new IllegalStateException(
                "Expected embedding dimension 768 but received "
                        + embedding.length
        );
    }

    String formattedEmbedding =
            vectorFormatter.format(embedding);

    chunkRepository.updateEmbedding(
            chunk.getId(),
            formattedEmbedding
    );
}

return savedDocument;
}

public void delete(
        UUID workspaceId,
        UUID documentId
) {
    workspaceService.findById(workspaceId);

    KnowledgeDocument document =
            repository.findById(documentId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Knowledge document not found."
                            )
                    );

    if (!document.getWorkspace()
            .getId()
            .equals(workspaceId)) {

        throw new IllegalArgumentException(
                "Knowledge document does not belong to this workspace."
        );
    }

    chunkRepository.deleteAllByDocumentId(
            documentId
    );

    repository.delete(document);
}


}