package com.isaacai.chat.service;

import com.isaacai.ai.client.AiStreamingClient;
import com.isaacai.title.AiTitleGenerator;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.message.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatStreamingServiceTest {

    @Mock
    private AiStreamingClient aiStreamingClient;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private AiTitleGenerator aiTitleGenerator;

    @Mock
    private ConversationService conversationService;

    @Mock
    private Message userMessage;

    @Mock
    private Conversation conversation;

    private ChatStreamingService chatStreamingService;

    @BeforeEach
    void setUp() {
        chatStreamingService = new ChatStreamingService(
                aiStreamingClient,
                chatSessionService,
                aiTitleGenerator,
                conversationService
        );
    }

    @Test
    void shouldStreamAndSaveAssistantMessage() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Hello"
        );

        List<Message> history = List.of(userMessage);

        PreparedChat preparedChat =
                new PreparedChat(userMessage, history);

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(aiStreamingClient.stream(history))
                .thenReturn(Flux.just("Hello", " Isaac"));

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenReturn(conversation);

        when(conversation.hasDefaultTitle())
                .thenReturn(false);

        StepVerifier.create(
                        chatStreamingService.stream(request)
                )
                .expectNext("Hello")
                .expectNext(" Isaac")
                .verifyComplete();

        verify(chatSessionService)
                .saveAssistantMessage(
                        request,
                        "Hello Isaac"
                );

        verify(conversationService)
                .findById(
                        workspaceId,
                        conversationId
                );
    }
}