package com.isaacai.ai.retrieval;

import com.isaacai.ai.embedding.EmbeddingClient;
import com.isaacai.ai.embedding.VectorFormatter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KnowledgeRetrieverTest {

    @Test
    void shouldRetrieveKnowledgeForWorkspace() {

        EmbeddingClient embeddingClient =
                mock(EmbeddingClient.class);

        VectorFormatter vectorFormatter =
                mock(VectorFormatter.class);

        KnowledgeRetrievalRepository repository =
                mock(KnowledgeRetrievalRepository.class);

        KnowledgeRetriever retriever =
                new KnowledgeRetriever(
                        embeddingClient,
                        vectorFormatter,
                        repository
                );

        UUID workspaceId = UUID.randomUUID();

        String query =
                "What does the document say about communication?";

        float[] embedding = new float[768];

        String formattedEmbedding =
                "[0.1,0.2,0.3]";

        List<RetrievedKnowledge> expected =
                List.of(
                        new RetrievedKnowledge(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "test.pdf",
                                3,
                                "Communication is important...",
                                0.15
                        )
                );

        when(
                embeddingClient.embed(query)
        ).thenReturn(embedding);

        when(
                vectorFormatter.format(embedding)
        ).thenReturn(formattedEmbedding);

        when(
                repository.findSimilar(
                        workspaceId,
                        formattedEmbedding,
                        5
                )
        ).thenReturn(List.of(expected.getFirst(),
                new RetrievedKnowledge(UUID.randomUUID(), UUID.randomUUID(), "irrelevant.pdf", 0, "Unrelated", 0.9)));

        List<RetrievedKnowledge> result =
                retriever.retrieve(
                        workspaceId,
                        query
                );

        assertEquals(1, result.size());
        assertEquals(
                "Communication is important...",
                result.getFirst().content()
        );

        verify(embeddingClient).embed(query);

        verify(repository).findSimilar(
                workspaceId,
                formattedEmbedding,
                5
        );
    }
}