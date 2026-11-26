
package com.isaacai.chat.service;

import com.isaacai.ai.client.AiStreamingClient;
import com.isaacai.ai.memory.MemoryCommandDetector;
import com.isaacai.ai.memory.MemoryContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeRetriever;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.memory.service.MemoryService;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import com.isaacai.title.AiTitleGenerator;
import com.isaacai.server.memory.model.MemoryCategory;
import com.isaacai.ai.memory.MemoryCommandHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import com.isaacai.ai.memory.UpdateMemoryCommand;
import com.isaacai.server.memory.model.Memory;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

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
    private WorkspaceService workspaceService;

    @Mock
    private KnowledgeRetriever knowledgeRetriever;

    @Mock
    private KnowledgeContextBuilder knowledgeContextBuilder;

    @Mock
    private ChatPromptBuilder chatPromptBuilder;

    @Mock
    private MemoryService memoryService;

    @Mock
    private MemoryContextBuilder memoryContextBuilder;

    @Mock
    private MemoryCommandDetector memoryCommandDetector;

    @Mock
    private Message userMessage;

    @Mock
    private Conversation conversation;

    @Mock
    private Workspace workspace;

    private ChatStreamingService chatStreamingService;

    @BeforeEach
    void setUp() {
        MemoryCommandHandler memoryCommandHandler =
                new MemoryCommandHandler(
                        memoryCommandDetector,
                        memoryService
                );

        chatStreamingService = new ChatStreamingService(
                aiStreamingClient,
                chatSessionService,
                aiTitleGenerator,
                conversationService,
                workspaceService,
                knowledgeRetriever,
                knowledgeContextBuilder,
                chatPromptBuilder,
                memoryService,
                memoryContextBuilder,
                memoryCommandHandler
        );
    }

    @Test
    void shouldStreamAndSaveAssistantMessage() {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String systemPrompt =
                "Act as a helpful personal assistant.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Hello"
        );

        List<Message> history = List.of(userMessage);

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        history
                );

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

        when(knowledgeContextBuilder.build(List.of()))
                .thenReturn("");

        when(memoryService.findAll(workspaceId))
                .thenReturn(List.of());

        when(memoryContextBuilder.build(List.of()))
                .thenReturn("");

        when(chatPromptBuilder.build(
                systemPrompt,
                "",
                ""
        )).thenReturn(systemPrompt);

        when(aiStreamingClient.stream(
                systemPrompt,
                history
        )).thenReturn(
                Flux.just("Hello", " Isaac")
        );

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

        verify(workspaceService)
                .findById(workspaceId);

        verify(knowledgeRetriever)
                .retrieve(
                        workspaceId,
                        request.message()
                );

        verify(knowledgeContextBuilder)
                .build(List.of());

        verify(memoryService)
                .findAll(workspaceId);

        verify(memoryContextBuilder)
                .build(List.of());

        verify(chatPromptBuilder)
                .build(
                        systemPrompt,
                        "",
                        ""
                );

        verify(aiStreamingClient)
                .stream(
                        systemPrompt,
                        history
                );

        verify(conversationService)
                .findById(
                        workspaceId,
                        conversationId
                );

        verify(memoryCommandDetector)
                .isRememberCommand(request.message());

        verify(memoryCommandDetector)
                .isForgetCommand(request.message());

        verify(memoryService, never())
                .forget(any(), anyString());
    }

    @Mock
    private Memory memory;

    @Test
    void shouldRememberExplicitMemoryWhenStreaming() {
        ChatRequest request = prepareCommand("Remember that I prefer Java.");
        when(memoryCommandDetector.isRememberCommand(request.message())).thenReturn(true);
        when(memoryCommandDetector.extractRememberContent(request.message())).thenReturn("I prefer Java.");
        when(memoryService.create(request.workspaceId(), "I prefer Java.",
                MemoryCategory.CONTEXT, request.conversationId())).thenReturn(memory);
        when(memory.getContent()).thenReturn("I prefer Java.");

        verifyCommandResponse(request, "I'll remember that: I prefer Java.");
        verify(memoryService).create(request.workspaceId(), "I prefer Java.",
                MemoryCategory.CONTEXT, request.conversationId());
        verifyNoMoreInteractions(memoryService);
    }

    @Test
    void shouldForgetExplicitMemoryWhenStreaming() {
        verifyForget(true, "I've removed that memory.");
    }

    @Test
    void shouldReportMissingMemoryWhenForgetting() {
        verifyForget(false, "I couldn't find a matching memory to remove.");
    }

    private void verifyForget(boolean forgotten, String expected) {
        ChatRequest request = prepareCommand("Forget that I prefer Java.");
        when(memoryCommandDetector.isForgetCommand(request.message())).thenReturn(true);
        when(memoryCommandDetector.extractForgetContent(request.message())).thenReturn("I prefer Java.");
        when(memoryService.forget(request.workspaceId(), "I prefer Java.")).thenReturn(forgotten);

        verifyCommandResponse(request, expected);
        verify(memoryService).forget(request.workspaceId(), "I prefer Java.");
        verifyNoMoreInteractions(memoryService);
    }

    @Test
    void shouldUpdateExplicitMemoryWhenStreaming() {
        ChatRequest request = prepareUpdate();
        when(memoryService.update(request.workspaceId(), "I prefer Java.", "I prefer Python."))
                .thenReturn(memory);
        when(memory.getContent()).thenReturn("I prefer Python.");

        verifyCommandResponse(request, "Your memory has been updated: I prefer Python.");
        verify(memoryService).update(request.workspaceId(), "I prefer Java.", "I prefer Python.");
        verifyNoMoreInteractions(memoryService);
    }

    @Test
    void shouldRejectUpdateWhenOriginalMemoryDoesNotExist() {
        verifyRejectedUpdate(new IllegalArgumentException("Original memory not found."));
    }

    @Test
    void shouldRejectUpdateWhenReplacementAlreadyExists() {
        verifyRejectedUpdate(new IllegalArgumentException("Another memory already contains the new content."));
    }

    @Test
    void shouldRejectUpdateWhenMultipleMemoriesMatch() {
        verifyRejectedUpdate(new IllegalStateException("Multiple memories match the original content."));
    }

    private void verifyRejectedUpdate(RuntimeException exception) {
        ChatRequest request = prepareUpdate();
        when(memoryService.update(request.workspaceId(), "I prefer Java.", "I prefer Python."))
                .thenThrow(exception);

        verifyCommandResponse(request, "Unable to update your memory. " + exception.getMessage());
        verify(memoryService).update(request.workspaceId(), "I prefer Java.", "I prefer Python.");
        verifyNoMoreInteractions(memoryService);
    }

    private ChatRequest prepareUpdate() {
        ChatRequest request = prepareCommand(
                "Update my memory from \"I prefer Java.\" to \"I prefer Python.\".");
        when(memoryCommandDetector.extractUpdateCommand(request.message()))
                .thenReturn(Optional.of(new UpdateMemoryCommand("I prefer Java.", "I prefer Python.")));
        return request;
    }

    private ChatRequest prepareCommand(String message) {
        ChatRequest request = new ChatRequest(UUID.randomUUID(), UUID.randomUUID(), message);
        when(chatSessionService.prepareConversation(request))
                .thenReturn(new PreparedChat(userMessage, List.of(userMessage)));
        when(conversationService.findById(request.workspaceId(), request.conversationId()))
                .thenReturn(conversation);
        when(conversation.hasDefaultTitle()).thenReturn(false);
        return request;
    }

    private void verifyCommandResponse(ChatRequest request, String expected) {
        StepVerifier.create(chatStreamingService.stream(request))
                .expectNext(expected)
                .verifyComplete();
        verify(chatSessionService).saveAssistantMessage(request, expected);
        verifyNoInteractions(aiStreamingClient, workspaceService, knowledgeRetriever,
                knowledgeContextBuilder, memoryContextBuilder, chatPromptBuilder, aiTitleGenerator);
    }
}
