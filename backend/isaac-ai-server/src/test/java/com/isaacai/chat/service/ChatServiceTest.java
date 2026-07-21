package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.service.MessageService;
import com.isaacai.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AiChatClient aiChatClient;

    @Mock
    private MessageService messageService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                aiChatClient,
                messageService
        );
    }

    @Test
    void shouldCreateUserMessageGenerateAnswerAndCreateAssistantMessage() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Hello Isaac AI"
        );
        var workspace = TestDataFactory.workspace();
var conversation =
        TestDataFactory.conversation(workspace, "Test conversation");

        Message userMessage =
                TestDataFactory.userMessage(conversation, "Hello Isaac AI");

        Message assistantMessage =
                TestDataFactory.assistantMessage(conversation, "Hello Isaac");

        List<Message> history = List.of(userMessage);

        when(messageService.createUserMessage(
                workspaceId,
                conversationId,
                request.message()
        )).thenReturn(userMessage);

        when(messageService.findConversationMessages(
                workspaceId,
                conversationId
        )).thenReturn(history);

        when(aiChatClient.chat(history))
                .thenReturn("Hello Isaac");

        when(messageService.createAssistantMessage(
                workspaceId,
                conversationId,
                "Hello Isaac"
        )).thenReturn(assistantMessage);

        ChatResponse response = chatService.chat(request);

        assertThat(response.conversationId())
                .isEqualTo(conversationId);

        assertThat(response.userMessageId())
                .isEqualTo(userMessage.getId());

        assertThat(response.assistantMessageId())
                .isEqualTo(assistantMessage.getId());

        assertThat(response.answer())
                .isEqualTo("Hello Isaac");

        var order = inOrder(messageService, aiChatClient);

        order.verify(messageService).createUserMessage(
                workspaceId,
                conversationId,
                request.message()
        );

        order.verify(messageService).findConversationMessages(
                workspaceId,
                conversationId
        );

        order.verify(aiChatClient).chat(history);

        order.verify(messageService).createAssistantMessage(
                workspaceId,
                conversationId,
                "Hello Isaac"
        );
    }
}