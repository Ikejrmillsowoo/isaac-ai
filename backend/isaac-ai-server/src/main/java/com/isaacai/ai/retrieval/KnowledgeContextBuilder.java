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

        for (RetrievedKnowledge knowledge
                : retrievedKnowledge) {

            context.append("[Document: ")
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