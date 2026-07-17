package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    @Test
    void shouldGenerateResponseUsingAiClient() {
        AiChatClient aiChatClient = mock(AiChatClient.class);

        when(aiChatClient.generate("Hello"))
                .thenReturn("Hello, Isaac.");

        ChatService chatService = new ChatService(aiChatClient);

        String result = chatService.generateResponse("Hello");

        assertEquals("Hello, Isaac.", result);
        verify(aiChatClient).generate("Hello");
    }
}