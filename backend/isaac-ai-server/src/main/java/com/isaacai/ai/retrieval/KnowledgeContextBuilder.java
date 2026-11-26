package com.isaacai.ai.retrieval;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeContextBuilder {

    public String build(
            List<RetrievedKnowledge> retrievedKnowledge
    ) {

        if (retrievedKnowledge == null ||
                retrievedKnowledge.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();

        context.append("""
                Relevant knowledge from this workspace:

                """);

        int index = 0;
        for (RetrievedKnowledge knowledge
                : retrievedKnowledge) {

            if (knowledge.content() == null || knowledge.content().isBlank()) continue;
            String heading = "[K" + (++index) + "] ";
            if (context.length() + heading.length() + knowledge.documentName().length() + knowledge.content().length() + 64 > 4000) continue;
            context.append(heading).append("[Document: ")
                    .append(knowledge.documentName())
                    .append(", Chunk: ")
                    .append(knowledge.chunkIndex())
                    .append("]\n");

            context.append(knowledge.content())
                    .append("\n\n");
        }

        return context.toString().trim();
    }
}