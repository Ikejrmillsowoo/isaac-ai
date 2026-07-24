package com.isaacai.chat.service;

import com.isaacai.ai.client.AiStreamingClient;
import com.isaacai.chat.dto.ChatRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatStreamingService {

    private final AiStreamingClient aiStreamingClient;
    private final ChatSessionService chatSessionService;

    public ChatStreamingService(
            AiStreamingClient aiStreamingClient,
            ChatSessionService chatSessionService
    ) {
        this.aiStreamingClient = aiStreamingClient;
        this.chatSessionService = chatSessionService;
    }

    public Flux<String> stream(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

        StringBuilder assistantResponse =
                new StringBuilder();

        return aiStreamingClient
                .stream(preparedChat.history())
                .doOnNext(assistantResponse::append)
                .doOnComplete(() -> {
                    String answer = assistantResponse.toString();

                    if (!answer.isBlank()) {
                        chatSessionService.saveAssistantMessage(
                                request,
                                answer
                        );
                    }
                });
    }
}