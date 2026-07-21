package com.isaacai.ai.client;

import com.isaacai.server.message.model.Message;

import java.util.List;

public interface AiChatClient {

    String chat(List<Message> history);
}