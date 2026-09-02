package com.isaacai.ai.embedding;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OllamaEmbeddingClientTest {

    @Test
    void shouldGenerateEmbedding() {

        EmbeddingModel embeddingModel =
                mock(EmbeddingModel.class);

        float[] expectedEmbedding =
                new float[768];

        when(
                embeddingModel.embed(
                        "Healthy relationships require communication."
                )
        ).thenReturn(expectedEmbedding);

        OllamaEmbeddingClient client =
                new OllamaEmbeddingClient(
                        embeddingModel
                );

        float[] result =
                client.embed(
                        "Healthy relationships require communication."
                );

        assertNotNull(result);
        assertEquals(768, result.length);

        verify(embeddingModel).embed(
                "Healthy relationships require communication."
        );
    }
}