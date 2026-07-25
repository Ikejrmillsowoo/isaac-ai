package com.isaacai.ai.title;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import com.isaacai.title.AiTitleGenerator;

@Component
public class OllamaAiTitleGenerator
        implements AiTitleGenerator {

    private final ChatClient chatClient;

    public OllamaAiTitleGenerator(
            ChatClient.Builder builder
    ) {
        this.chatClient = builder.build();
    }

    @Override
    public String generateTitle(
            String firstMessage
    ) {

        return chatClient
                .prompt()
                .system("""
                        Generate a concise conversation title.

                        Rules:
                        - 2 to 5 words.
                        - No quotation marks.
                        - No punctuation.
                        - Title Case.
                        - Return only the title.
                        """)
                .user(firstMessage)
                .call()
                .content()
                .trim();
    }
}