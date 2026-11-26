package com.isaacai.ai.retrieval;

import java.util.List;

public final class KnowledgeSources {
    private KnowledgeSources() {}

    public static String footer(List<RetrievedKnowledge> retrieved) {
        if (retrieved == null || retrieved.isEmpty()) return "";
        StringBuilder context = new StringBuilder("Relevant knowledge from this workspace:\n\n");
        StringBuilder sources = new StringBuilder();
        int index = 0;
        for (RetrievedKnowledge item : retrieved) {
            if (item.content() == null || item.content().isBlank()) continue;
            String label = "[K" + (index + 1) + "] ";
            if (context.length() + label.length() + item.documentName().length() + item.content().length() + 64 > 4000) continue;
            index++;
            context.append(label).append("[Document: ").append(item.documentName()).append(", Chunk: ").append(item.chunkIndex()).append("]\n").append(item.content()).append("\n\n");
            sources.append("\n- ").append(label).append(item.documentName().replaceAll("[\\r\\n]", " ")).append(" (chunk ").append(item.chunkIndex()).append(")");
        }
        return index == 0 ? "" : "\n\n**Retrieved sources (not verified claim citations):**\n" + sources;
    }
}
