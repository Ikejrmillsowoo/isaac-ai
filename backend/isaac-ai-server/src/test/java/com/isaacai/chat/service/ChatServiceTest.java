package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.message.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.isaacai.title.AiTitleGenerator;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

@Mock
private AiTitleGenerator aiTitleGenerator;

@Mock
private ConversationService conversationService;

@Mock
private Conversation conversation;

    @Mock
    private AiChatClient aiChatClient;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private Message userMessage;

    @Mock
    private Message assistantMessage;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                aiChatClient,
            chatSessionService,
            aiTitleGenerator,
            conversationService
        );
    }

    @Test
    void shouldPrepareConversationGenerateAnswerAndSaveAssistantMessage() {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID userMessageId = UUID.randomUUID();
        UUID assistantMessageId = UUID.randomUUID();

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Hello Isaac AI"
        );

        List<Message> history =
                List.of(userMessage);

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        history
                );

        when(userMessage.getId())
                .thenReturn(userMessageId);

        when(assistantMessage.getId())
                .thenReturn(assistantMessageId);

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(aiChatClient.chat(history))
                .thenReturn("Hello Isaac!");

        when(chatSessionService.saveAssistantMessage(
                request,
                "Hello Isaac!"
        )).thenReturn(assistantMessage);

        when(conversationService.findById(
        workspaceId,
        conversationId
)).thenReturn(conversation);

when(conversation.hasDefaultTitle())
        .thenReturn(false);

        ChatResponse response =
                chatService.chat(request);

        assertThat(response.conversationId())
                .isEqualTo(conversationId);

        assertThat(response.userMessageId())
                .isEqualTo(userMessageId);

        assertThat(response.assistantMessageId())
                .isEqualTo(assistantMessageId);

        assertThat(response.answer())
                .isEqualTo("Hello Isaac!");

        InOrder order =
                inOrder(
                        chatSessionService,
                        aiChatClient
                );

        verify(conversationService)
        .findById(workspaceId, conversationId);

        verify(conversation)
        .hasDefaultTitle();

        order.verify(chatSessionService)
                .prepareConversation(request);

        order.verify(aiChatClient)
                .chat(history);

        order.verify(chatSessionService)
                .saveAssistantMessage(
                        request,
                        "Hello Isaac!"
                );

        // verifyNoMoreInteractions(
        //         chatSessionService,
        //         aiChatClient
        // );
    }
}