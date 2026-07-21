package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final AiChatClient aiChatClient;

    public ChatService(AiChatClient aiChatClient) {
        this.aiChatClient = aiChatClient;
    }

    public ChatResponse chat(ChatRequest request) {
        return aiChatClient.generate(message);
    }
}