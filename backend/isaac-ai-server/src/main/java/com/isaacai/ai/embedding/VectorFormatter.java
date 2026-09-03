package com.isaacai.ai.embedding;

import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class VectorFormatter {

    public String format(float[] vector) {

        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException(
                    "Embedding vector cannot be empty."
            );
        }

        StringJoiner joiner =
                new StringJoiner(",", "[", "]");

        for (float value : vector) {
            joiner.add(Float.toString(value));
        }

        return joiner.toString();
    }
}