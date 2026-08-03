package com.isaacai.ai.client;

import com.isaacai.server.message.model.Message;
import reactor.core.publisher.Flux;
import com.isaacai.ai.mapper.AiMessageMapper;

import java.util.List;

public interface AiChatClient {

    String chat(
        String systemPrompt,
        List<Message> history
        );
}