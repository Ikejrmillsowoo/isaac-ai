package com.isaacai.ai.client;

import com.isaacai.server.message.model.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiStreamingClient {

    Flux<String> stream(List<Message> history);

}