package com.isaacai.ai.client;

import com.isaacai.ai.mapper.AiMessageMapper;
import com.isaacai.server.message.model.Message;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import com.isaacai.ai.mapper.AiMessageMapper;


import java.util.List;

@Component
public class OllamaAiStreamingClient implements AiStreamingClient {

    private final ChatClient chatClient;
    private final OllamaAiChatClient toolClient;
    private final com.isaacai.tools.TaskActions actions;
    private final AiMessageMapper aiMessageMapper;

    public OllamaAiStreamingClient(
            ChatClient.Builder builder,
            AiMessageMapper aiMessageMapper, OllamaAiChatClient toolClient, com.isaacai.tools.TaskActions actions
    ) {
        this.chatClient = builder.build();
        this.toolClient=toolClient;
        this.actions=actions;

        this.aiMessageMapper = aiMessageMapper;
    }

    @Override
    public Flux<String> streamWithTools(String systemPrompt,List<Message> history,java.util.UUID workspace) {
        if (!actions.enabled(workspace)) return stream(systemPrompt,history);
        return Flux.defer(() -> reactor.core.publisher.Mono.justOrEmpty(toolClient.chatWithTools(systemPrompt,history,workspace)).flux())
            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    @Override
    public Flux<String> stream(String systemPrompt,List<Message> history) {

        List<org.springframework.ai.chat.messages.Message> aiMessages =
                HistoryWindow.select(history, systemPrompt).stream()
                        .map(aiMessageMapper::map)
                        .toList();

        return chatClient
                .prompt()
                .system(systemPrompt)
                .messages(aiMessages)
                .stream()
                .content();
    }
}