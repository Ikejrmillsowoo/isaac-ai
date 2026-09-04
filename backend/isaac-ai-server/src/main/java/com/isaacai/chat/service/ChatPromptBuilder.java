package com.isaacai.chat.service;

import org.springframework.stereotype.Component;

@Component
public class ChatPromptBuilder {

    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are Isaac AI, a private personal assistant.
            Be clear, practical, and accurate.
            Do not invent facts.
            """;

    public String build(
            String systemPrompt,
            String knowledgeContext
    ) {

        String resolvedSystemPrompt =
                resolveSystemPrompt(systemPrompt);

        if (knowledgeContext == null ||
                knowledgeContext.isBlank()) {
            return resolvedSystemPrompt;
        }

        return """
                %s

                You have access to relevant knowledge retrieved
                from documents in this workspace.

                Use the workspace knowledge below when it is
                relevant to the user's question.

                Do not claim that the knowledge says something
                that is not supported by the provided text.

                If the provided knowledge does not contain enough
                information to answer the question, say so rather
                than inventing information.

                --- WORKSPACE KNOWLEDGE ---

                %s

                --- END WORKSPACE KNOWLEDGE ---
                """.formatted(
                resolvedSystemPrompt,
                knowledgeContext
        );
    }

    private String resolveSystemPrompt(
            String systemPrompt
    ) {

        if (systemPrompt == null ||
                systemPrompt.isBlank()) {
            return DEFAULT_SYSTEM_PROMPT;
        }

        return systemPrompt.trim();
    }
}