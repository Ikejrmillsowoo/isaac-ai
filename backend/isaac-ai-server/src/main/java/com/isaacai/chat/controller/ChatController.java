package com.isaacai.chat.controller;

import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.isaacai.chat.service.ChatStreamingService;

import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatStreamingService chatStreamingService;

    public ChatController(ChatService chatService, ChatStreamingService chatStreamingService) {
        this.chatService = chatService;
        this.chatStreamingService = chatStreamingService;
    }

    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request
    ) {
        return chatService.chat(request);
    }

    @PostMapping(
        value = "/stream",
        produces = MediaType.TEXT_PLAIN_VALUE
)
public Flux<String> stream(
        @RequestBody ChatRequest request
) {
    return chatStreamingService.stream(request);
}
}