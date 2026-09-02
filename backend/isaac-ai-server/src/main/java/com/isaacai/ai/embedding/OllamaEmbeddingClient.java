package com.isaacai.ai.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

@Component
public class OllamaEmbeddingClient
        implements EmbeddingClient {

    private final EmbeddingModel embeddingModel;

    public OllamaEmbeddingClient(
            EmbeddingModel embeddingModel
    ) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] embed(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text to embed cannot be blank."
            );
        }

        return embeddingModel.embed(text);
    }
}