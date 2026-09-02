package com.isaacai.server.knowledge.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentChunkingService {

    private static final int CHUNK_SIZE = 1500;
    private static final int CHUNK_OVERLAP = 200;

    public List<String> chunk(String text) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalizedText = text.trim();

        List<String> chunks = new ArrayList<>();

        int start = 0;

        while (start < normalizedText.length()) {

            int end = Math.min(
                    start + CHUNK_SIZE,
                    normalizedText.length()
            );

            String chunk =
                    normalizedText.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end == normalizedText.length()) {
                break;
            }

            start = end - CHUNK_OVERLAP;
        }

        return chunks;
    }
}