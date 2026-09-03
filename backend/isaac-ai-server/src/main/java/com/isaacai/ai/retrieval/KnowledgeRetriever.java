package com.isaacai.ai.retrieval;

import com.isaacai.ai.embedding.EmbeddingClient;
import com.isaacai.ai.embedding.VectorFormatter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeRetriever {

    private static final int DEFAULT_LIMIT = 5;

    private final EmbeddingClient embeddingClient;
    private final VectorFormatter vectorFormatter;
    private final KnowledgeRetrievalRepository retrievalRepository;

    public KnowledgeRetriever(
            EmbeddingClient embeddingClient,
            VectorFormatter vectorFormatter,
            KnowledgeRetrievalRepository retrievalRepository
    ) {
        this.embeddingClient = embeddingClient;
        this.vectorFormatter = vectorFormatter;
        this.retrievalRepository = retrievalRepository;
    }

    public List<RetrievedKnowledge> retrieve(
            UUID workspaceId,
            String query
    ) {

        if (workspaceId == null) {
            throw new IllegalArgumentException(
                    "Workspace ID is required."
            );
        }

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Knowledge query cannot be blank."
            );
        }

        float[] embedding =
                embeddingClient.embed(query);

        if (embedding.length != 768) {
            throw new IllegalStateException(
                    "Expected embedding dimension 768 but received "
                            + embedding.length
            );
        }

        String formattedEmbedding =
                vectorFormatter.format(embedding);

        return retrievalRepository.findSimilar(
                workspaceId,
                formattedEmbedding,
                DEFAULT_LIMIT
        );
    }
}