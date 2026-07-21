package com.isaacai.ai.client;

public interface AiChatClient {

    String chat(List<ChatMessage> history);
}