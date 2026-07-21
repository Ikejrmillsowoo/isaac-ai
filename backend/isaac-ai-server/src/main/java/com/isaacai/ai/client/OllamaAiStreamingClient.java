package com.isaacai.ai.client;

import com.isaacai.ai.mapper.AiMessageMapper;
import com.isaacai.server.message.model.Message;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class OllamaAiStreamingClient implements AiStreamingClient {

    private final ChatClient chatClient;
    private final AiMessageMapper aiMessageMapper;

    public OllamaAiStreamingClient(
            ChatClient.Builder builder,
            AiMessageMapper aiMessageMapper
    ) {
        this.chatClient = builder
                .defaultSystem("""
                        You are Isaac AI, a private personal assistant.
                        Be clear, practical, and accurate.
                        Do not invent facts.
                        """)
                .build();

        this.aiMessageMapper = aiMessageMapper;
    }

    @Override
    public Flux<String> stream(List<Message> history) {

        List<org.springframework.ai.chat.messages.Message> aiMessages =
                history.stream()
                        .map(aiMessageMapper::map)
                        .toList();

        return chatClient
                .prompt()
                .messages(aiMessages)
                .stream()
                .content();
    }
}