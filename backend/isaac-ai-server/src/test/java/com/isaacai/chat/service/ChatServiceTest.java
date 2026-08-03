package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import com.isaacai.title.AiTitleGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AiChatClient aiChatClient;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private AiTitleGenerator aiTitleGenerator;

    @Mock
    private ConversationService conversationService;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private Workspace workspace;

    @Mock
    private Conversation conversation;

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
                conversationService,
                workspaceService
        );
    }

    @Test
    void shouldPrepareConversationGenerateAnswerAndSaveAssistantMessage() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID userMessageId = UUID.randomUUID();
        UUID assistantMessageId = UUID.randomUUID();

        String systemPrompt =
                "Act as a helpful personal assistant.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Hello Isaac AI"
        );

        List<Message> history = List.of(userMessage);

        PreparedChat preparedChat = new PreparedChat(
                userMessage,
                history
        );

        when(userMessage.getId())
                .thenReturn(userMessageId);

        when(assistantMessage.getId())
                .thenReturn(assistantMessageId);

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        when(workspace.getSystemPrompt())
                .thenReturn(systemPrompt);

        when(aiChatClient.chat(
                systemPrompt,
                history
        )).thenReturn("Hello Isaac!");

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

        ChatResponse response = chatService.chat(request);

        assertThat(response.conversationId())
                .isEqualTo(conversationId);

        assertThat(response.userMessageId())
                .isEqualTo(userMessageId);

        assertThat(response.assistantMessageId())
                .isEqualTo(assistantMessageId);

        assertThat(response.answer())
                .isEqualTo("Hello Isaac!");

        InOrder order = inOrder(
                chatSessionService,
                workspaceService,
                aiChatClient
        );

        order.verify(chatSessionService)
                .prepareConversation(request);

        order.verify(workspaceService)
                .findById(workspaceId);

        order.verify(aiChatClient)
                .chat(
                        systemPrompt,
                        history
                );

        order.verify(chatSessionService)
                .saveAssistantMessage(
                        request,
                        "Hello Isaac!"
                );

        verify(conversationService)
                .findById(
                        workspaceId,
                        conversationId
                );

        verify(conversation)
                .hasDefaultTitle();

        verify(aiTitleGenerator, never())
                .generateTitle(request.message());

        verify(conversationService, never())
                .rename(
                        workspaceId,
                        conversationId,
                        "Hello Isaac AI"
                );
    }

    @Test
    void shouldGenerateConversationTitleForNewConversation() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID userMessageId = UUID.randomUUID();
        UUID assistantMessageId = UUID.randomUUID();

        String systemPrompt =
                "Act as a senior Java interview coach.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Help me prepare for a Java interview"
        );

        List<Message> history = List.of(userMessage);

        PreparedChat preparedChat = new PreparedChat(
                userMessage,
                history
        );

        when(userMessage.getId())
                .thenReturn(userMessageId);

        when(assistantMessage.getId())
                .thenReturn(assistantMessageId);

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        when(workspace.getSystemPrompt())
                .thenReturn(systemPrompt);

        when(aiChatClient.chat(
                systemPrompt,
                history
        )).thenReturn("Here is a study plan...");

        when(chatSessionService.saveAssistantMessage(
                request,
                "Here is a study plan..."
        )).thenReturn(assistantMessage);

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenReturn(conversation);

        when(conversation.hasDefaultTitle())
                .thenReturn(true);

        when(aiTitleGenerator.generateTitle(
                request.message()
        )).thenReturn("Java Interview Preparation");

        ChatResponse response = chatService.chat(request);

        assertThat(response.conversationId())
                .isEqualTo(conversationId);

        assertThat(response.answer())
                .isEqualTo("Here is a study plan...");

        verify(workspaceService)
                .findById(workspaceId);

        verify(aiChatClient)
                .chat(
                        systemPrompt,
                        history
                );

        verify(aiTitleGenerator)
                .generateTitle(request.message());

        verify(conversationService)
                .rename(
                        workspaceId,
                        conversationId,
                        "Java Interview Preparation"
                );
    }
}