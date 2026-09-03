package com.isaacai.ai.retrieval;

import java.util.UUID;

public record RetrievedKnowledge(
        UUID chunkId,
        UUID documentId,
        String documentName,
        int chunkIndex,
        String content,
        double distance
) {
}