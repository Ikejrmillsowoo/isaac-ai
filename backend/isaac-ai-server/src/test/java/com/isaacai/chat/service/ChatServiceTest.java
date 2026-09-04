package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.ai.retrieval.KnowledgeContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeRetriever;
import com.isaacai.ai.retrieval.RetrievedKnowledge;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    private KnowledgeContextBuilder knowledgeContextBuilder;

    @Mock
    private KnowledgeRetriever knowledgeRetriever;

    @Mock
    private ChatPromptBuilder chatPromptBuilder;

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
                workspaceService,
                knowledgeContextBuilder,
                knowledgeRetriever,
                chatPromptBuilder
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

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        when(workspace.getSystemPrompt())
                .thenReturn(systemPrompt);

        when(knowledgeRetriever.retrieve(
                workspaceId,
                request.message()
        )).thenReturn(List.of());

        when(knowledgeContextBuilder.build(
                List.of()
        )).thenReturn("");

        when(chatPromptBuilder.build(
                systemPrompt,
                ""
        )).thenReturn(systemPrompt);

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

        verify(knowledgeRetriever)
                .retrieve(
                        workspaceId,
                        request.message()
                );

        verify(knowledgeContextBuilder)
                .build(List.of());

        verify(chatPromptBuilder)
                .build(
                        systemPrompt,
                        ""
                );

        verify(aiChatClient)
                .chat(
                        systemPrompt,
                        history
                );

        verify(chatSessionService)
                .saveAssistantMessage(
                        request,
                        "Hello Isaac!"
                );

        verify(aiTitleGenerator, never())
                .generateTitle(request.message());
    }

    @Test
    void shouldGenerateConversationTitleForNewConversation() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String systemPrompt =
                "Act as a senior Java interview coach.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Help me prepare for a Java interview"
        );

        List<Message> history =
                List.of(userMessage);

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        history
                );

        when(userMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(assistantMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        when(workspace.getSystemPrompt())
                .thenReturn(systemPrompt);

        when(knowledgeRetriever.retrieve(
                workspaceId,
                request.message()
        )).thenReturn(List.of());

        when(knowledgeContextBuilder.build(
                List.of()
        )).thenReturn("");

        when(chatPromptBuilder.build(
                systemPrompt,
                ""
        )).thenReturn(systemPrompt);

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
        )).thenReturn(
                "Java Interview Preparation"
        );

        ChatResponse response =
                chatService.chat(request);

        assertThat(response.answer())
                .isEqualTo("Here is a study plan...");

        verify(aiTitleGenerator)
                .generateTitle(request.message());

        verify(conversationService)
                .rename(
                        workspaceId,
                        conversationId,
                        "Java Interview Preparation"
                );
    }

    @Test
    void shouldIncludeRetrievedKnowledgeInSystemPrompt() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String systemPrompt =
                "You are a helpful research assistant.";

        String knowledgeContext =
                """
                Relevant knowledge from this workspace:

                [Document: SMUS Manuscript.pdf, Chunk: 22]
                Participants rated 31 social media activities
                on a nine-point Likert scale.
                """.trim();

        String augmentedPrompt =
                systemPrompt
                        + "\n\n"
                        + knowledgeContext;

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "How was social media use measured?"
        );

        List<Message> history =
                List.of(userMessage);

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        history
                );

        RetrievedKnowledge retrievedKnowledge =
                new RetrievedKnowledge(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "SMUS Manuscript.pdf",
                        22,
                        "Participants rated 31 social media activities "
                                + "on a nine-point Likert scale.",
                        0.23
                );

        when(userMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(assistantMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        when(workspace.getSystemPrompt())
                .thenReturn(systemPrompt);

        when(knowledgeRetriever.retrieve(
                workspaceId,
                request.message()
        )).thenReturn(
                List.of(retrievedKnowledge)
        );

        when(knowledgeContextBuilder.build(
                List.of(retrievedKnowledge)
        )).thenReturn(knowledgeContext);

        when(chatPromptBuilder.build(
                systemPrompt,
                knowledgeContext
        )).thenReturn(augmentedPrompt);

        when(aiChatClient.chat(
                augmentedPrompt,
                history
        )).thenReturn(
                "The researchers measured social media use "
                        + "with 31 activities rated on a "
                        + "nine-point Likert scale."
        );

        when(chatSessionService.saveAssistantMessage(
                request,
                "The researchers measured social media use "
                        + "with 31 activities rated on a "
                        + "nine-point Likert scale."
        )).thenReturn(assistantMessage);

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenReturn(conversation);

        when(conversation.hasDefaultTitle())
                .thenReturn(false);

        ChatResponse response =
                chatService.chat(request);

        assertThat(response.answer())
                .contains("31 activities")
                .contains("nine-point Likert scale");

        verify(knowledgeRetriever)
                .retrieve(
                        workspaceId,
                        request.message()
                );

        verify(knowledgeContextBuilder)
                .build(
                        List.of(retrievedKnowledge)
                );

        verify(chatPromptBuilder)
                .build(
                        systemPrompt,
                        knowledgeContext
                );

        verify(aiChatClient)
                .chat(
                        augmentedPrompt,
                        history
                );
    }
}