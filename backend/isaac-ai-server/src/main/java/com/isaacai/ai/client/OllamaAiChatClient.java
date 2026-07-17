package com.isaacai.ai.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class OllamaAiChatClient implements AiChatClient {

    private final ChatClient chatClient;

    public OllamaAiChatClient(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                        You are Isaac AI, a private personal assistant.
                        Be clear, practical, and accurate.
                        Do not invent facts.
                        """)
                .build();
    }

    @Override
    public String generate(String prompt) {
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}