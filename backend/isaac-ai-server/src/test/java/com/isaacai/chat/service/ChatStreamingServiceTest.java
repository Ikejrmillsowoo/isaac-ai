
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

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
                memoryCommandDetector
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

    
@Test
void shouldRememberExplicitMemoryWhenStreaming() {

    UUID workspaceId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();

    String systemPrompt = "You are a helpful assistant.";

    ChatRequest request = new ChatRequest(
            workspaceId,
            conversationId,
            "Remember that I prefer Java."
    );

    List<Message> history = List.of(userMessage);

    PreparedChat preparedChat =
            new PreparedChat(userMessage, history);

    when(memoryCommandDetector.isRememberCommand(request.message()))
            .thenReturn(true);

    when(memoryCommandDetector.extractRememberContent(request.message()))
            .thenReturn("I prefer Java.");

    when(chatSessionService.prepareConversation(request))
            .thenReturn(preparedChat);

    when(workspaceService.findById(workspaceId))
            .thenReturn(workspace);

    when(workspace.getSystemPrompt())
            .thenReturn(systemPrompt);

    when(knowledgeRetriever.retrieve(
            workspaceId, request.message()
    )).thenReturn(List.of());

    when(knowledgeContextBuilder.build(List.of()))
            .thenReturn("");

    when(memoryService.findAll(workspaceId))
            .thenReturn(List.of());

    when(memoryContextBuilder.build(List.of()))
            .thenReturn("");

    when(chatPromptBuilder.build(systemPrompt, "", ""))
            .thenReturn(systemPrompt);

    when(aiStreamingClient.stream(systemPrompt, history))
            .thenReturn(Flux.just("I'll remember that."));

    when(conversationService.findById(workspaceId, conversationId))
            .thenReturn(conversation);

    when(conversation.hasDefaultTitle())
            .thenReturn(false);

    StepVerifier.create(chatStreamingService.stream(request))
            .expectNext("I'll remember that.")
            .verifyComplete();

    verify(memoryService).create(
            workspaceId,
            "I prefer Java.",
            MemoryCategory.CONTEXT,
            conversationId
    );
}


@Test
void shouldForgetExplicitMemoryWhenStreaming() {

    UUID workspaceId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();

    String systemPrompt = "You are a helpful assistant.";

    ChatRequest request = new ChatRequest(
            workspaceId,
            conversationId,
            "Forget that I prefer Java."
    );

    List<Message> history = List.of(userMessage);

    PreparedChat preparedChat =
            new PreparedChat(userMessage, history);

    when(memoryCommandDetector.isForgetCommand(request.message()))
            .thenReturn(true);

    when(memoryCommandDetector.extractForgetContent(request.message()))
            .thenReturn("I prefer Java.");

    when(memoryService.forget(
            workspaceId,
            "I prefer Java."
    )).thenReturn(true);

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
            Flux.just("Okay, I'll forget that.")
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
            .expectNext("Okay, I'll forget that.")
            .verifyComplete();

    verify(memoryService).forget(
            workspaceId,
            "I prefer Java."
    );

    verify(memoryService).findAll(workspaceId);
}


}
