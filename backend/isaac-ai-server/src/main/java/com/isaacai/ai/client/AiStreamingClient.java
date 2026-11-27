package com.isaacai.ai.client;

import com.isaacai.server.message.model.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiStreamingClient {

    default Flux<String> streamWithTools(String systemPrompt,List<Message> history,java.util.UUID workspace) {
        return stream(systemPrompt,history);
    }

    Flux<String> stream(        
        String systemPrompt,
        List<Message> history
    );

}