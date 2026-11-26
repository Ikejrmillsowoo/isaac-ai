package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.ai.memory.MemoryCommandHandler;
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
import com.isaacai.ai.memory.MemoryContextBuilder;
import com.isaacai.server.memory.service.MemoryService;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.ai.memory.MemoryCommandDetector;
import com.isaacai.server.memory.model.MemoryCategory;
import com.isaacai.ai.memory.UpdateMemoryCommand;
import java.util.Optional;




import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

    @Mock
private MemoryService memoryService;

@Mock
private MemoryContextBuilder memoryContextBuilder;

@Mock
private Memory memory;

@Mock
private MemoryCommandDetector memoryCommandDetector;

@Mock
private MemoryCategory memoryCategory;



    private ChatService chatService;

    @BeforeEach
    void setUp() {
        MemoryCommandHandler memoryCommandHandler =
                new MemoryCommandHandler(
                        memoryCommandDetector,
                        memoryService
                );
        chatService = new ChatService(
                aiChatClient,
                chatSessionService,
                aiTitleGenerator,
                conversationService,
                workspaceService,
                knowledgeContextBuilder,
                knowledgeRetriever,
                chatPromptBuilder,
                memoryContextBuilder,
                memoryService,
                memoryCommandHandler
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
                "",
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

        when(memoryService.findAll(workspaceId))
        .thenReturn(List.of());

when(memoryContextBuilder.build(
        List.of(), request.message()
)).thenReturn("");

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
                        "",
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
                "",
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
                knowledgeContext,
                ""
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
                        knowledgeContext,
                        ""
                );

        verify(aiChatClient)
                .chat(
                        augmentedPrompt,
                        history
                );
    }

    @Test
void shouldIncludeWorkspaceMemoryInSystemPrompt() {

    UUID workspaceId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();

    String systemPrompt =
            "You are a helpful personal assistant.";

    String memoryContext =
            """
            Relevant memories about the user:

            [PREFERENCE] User prefers Java examples over Python.
            """.trim();

    String augmentedPrompt =
            systemPrompt
                    + "\n\n"
                    + memoryContext;

    ChatRequest request = new ChatRequest(
            workspaceId,
            conversationId,
            "Show me how to check if a number is even."
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

    // This test is about memory, not knowledge.
    when(knowledgeRetriever.retrieve(
            workspaceId,
            request.message()
    )).thenReturn(List.of());

    when(knowledgeContextBuilder.build(
            List.of()
    )).thenReturn("");

    // Pretend this workspace has one stored memory.
    when(memoryService.findAll(workspaceId))
            .thenReturn(List.of(memory));

    // Pretend MemoryContextBuilder converts that memory
    // into the text that will be added to the prompt.
    when(memoryContextBuilder.build(
            List.of(memory), request.message()
    )).thenReturn(memoryContext);

    // ChatPromptBuilder receives the memory context.
    when(chatPromptBuilder.build(
            systemPrompt,
            "",
            memoryContext
    )).thenReturn(augmentedPrompt);

    // We are mocking the AI here.
    when(aiChatClient.chat(
            augmentedPrompt,
            history
    )).thenReturn(
            "Here is a Java example for checking "
                    + "whether a number is even."
    );

    when(chatSessionService.saveAssistantMessage(
            request,
            "Here is a Java example for checking "
                    + "whether a number is even."
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
            .contains("Java");

    verify(memoryService)
            .findAll(workspaceId);

    verify(memoryContextBuilder)
            .build(
                    List.of(memory), request.message()
            );

    verify(chatPromptBuilder)
            .build(
                    systemPrompt,
                    "",
                    memoryContext
            );

    verify(aiChatClient)
            .chat(
                    augmentedPrompt,
                    history
            );
}

    @Test
    void shouldSaveExplicitRememberCommand() {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String memoryContent =
                "my favorite programming language is Java.";

        String expectedAnswer =
                "I'll remember that: " + memoryContent;

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Remember that " + memoryContent
        );

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        List.of(userMessage)
                );

        when(userMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(assistantMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(memoryCommandDetector.isRememberCommand(
                request.message()
        )).thenReturn(true);

        when(memoryCommandDetector.extractRememberContent(
                request.message()
        )).thenReturn(memoryContent);

        when(memory.getContent())
                .thenReturn(memoryContent);

        when(memoryService.create(
                workspaceId,
                memoryContent,
                MemoryCategory.CONTEXT,
                conversationId
        )).thenReturn(memory);

        when(chatSessionService.saveAssistantMessage(
                request,
                expectedAnswer
        )).thenReturn(assistantMessage);

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenReturn(conversation);

        when(conversation.hasDefaultTitle())
                .thenReturn(false);

        ChatResponse response = chatService.chat(request);

        assertThat(response.answer())
                .isEqualTo(expectedAnswer);

        verify(memoryService).create(
                workspaceId,
                memoryContent,
                MemoryCategory.CONTEXT,
                conversationId
        );

        verify(chatSessionService)
                .saveAssistantMessage(request, expectedAnswer);

        verifyNoInteractions(
                aiChatClient,
                knowledgeRetriever,
                knowledgeContextBuilder,
                memoryContextBuilder,
                chatPromptBuilder,
                workspaceService
        );

        verifyNoMoreInteractions(memoryService);
    }


    @Test
    void shouldForgetExplicitMemoryCommand() {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String memoryContent = "I prefer Java.";
        String expectedAnswer = "I've removed that memory.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Forget that " + memoryContent
        );

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        List.of(userMessage)
                );

        when(userMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(assistantMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(memoryCommandDetector.isForgetCommand(
                request.message()
        )).thenReturn(true);

        when(memoryCommandDetector.extractForgetContent(
                request.message()
        )).thenReturn(memoryContent);

        when(memoryService.forget(
                workspaceId,
                memoryContent
        )).thenReturn(true);

        when(chatSessionService.saveAssistantMessage(
                request,
                expectedAnswer
        )).thenReturn(assistantMessage);

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenReturn(conversation);

        when(conversation.hasDefaultTitle())
                .thenReturn(false);

        ChatResponse response = chatService.chat(request);

        assertThat(response.answer())
                .isEqualTo(expectedAnswer);

        verify(memoryService).forget(
                workspaceId,
                memoryContent
        );

        verify(chatSessionService)
                .saveAssistantMessage(request, expectedAnswer);

        verifyNoInteractions(
                aiChatClient,
                knowledgeRetriever,
                knowledgeContextBuilder,
                memoryContextBuilder,
                chatPromptBuilder,
                workspaceService
        );

        verifyNoMoreInteractions(memoryService);
    }


    @Test
    void shouldRejectUpdateWhenOriginalMemoryDoesNotExist() {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String originalContent = "I prefer Java.";
        String newContent = "I prefer Python.";

        String expectedAnswer =
                "Unable to update your memory. Original memory not found.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Update my memory from \"" + originalContent
                        + "\" to \"" + newContent + "\"."
        );

        UpdateMemoryCommand updateCommand =
                new UpdateMemoryCommand(
                        originalContent,
                        newContent
                );

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        List.of(userMessage)
                );

        when(userMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(assistantMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(memoryCommandDetector.extractUpdateCommand(
                request.message()
        )).thenReturn(Optional.of(updateCommand));

        // Simulate a memory that does not exist
        when(memoryService.update(
                workspaceId,
                originalContent,
                newContent
        )).thenThrow(
                new IllegalArgumentException(
                        "Original memory not found."
                )
        );

        when(chatSessionService.saveAssistantMessage(
                request,
                expectedAnswer
        )).thenReturn(assistantMessage);

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenReturn(conversation);

        when(conversation.hasDefaultTitle())
                .thenReturn(false);

        // Execute
        ChatResponse response = chatService.chat(request);

        // Verify the error response
        assertThat(response.answer())
                .isEqualTo(expectedAnswer);

        verify(memoryService).update(
                workspaceId,
                originalContent,
                newContent
        );

        verify(chatSessionService)
                .saveAssistantMessage(request, expectedAnswer);

        verifyNoInteractions(
                aiChatClient,
                knowledgeRetriever,
                knowledgeContextBuilder,
                memoryContextBuilder,
                chatPromptBuilder,
                workspaceService
        );

        verifyNoMoreInteractions(memoryService);
    }



    @Test
    void shouldRejectUpdateWhenReplacementAlreadyExists() {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String originalContent = "I prefer Java.";
        String newContent = "I prefer Python.";

        String expectedAnswer =
                "Unable to update your memory. "
                        + "Another memory already contains the new content.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Update my memory from \"" + originalContent
                        + "\" to \"" + newContent + "\"."
        );

        UpdateMemoryCommand updateCommand =
                new UpdateMemoryCommand(
                        originalContent,
                        newContent
                );

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        List.of(userMessage)
                );

        when(userMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(assistantMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(memoryCommandDetector.extractUpdateCommand(
                request.message()
        )).thenReturn(Optional.of(updateCommand));

        when(memoryService.update(
                workspaceId,
                originalContent,
                newContent
        )).thenThrow(
                new IllegalArgumentException(
                        "Another memory already contains the new content."
                )
        );

        when(chatSessionService.saveAssistantMessage(
                request,
                expectedAnswer
        )).thenReturn(assistantMessage);

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenReturn(conversation);

        when(conversation.hasDefaultTitle())
                .thenReturn(false);

        ChatResponse response = chatService.chat(request);

        assertThat(response.answer())
                .isEqualTo(expectedAnswer);

        verify(memoryService).update(
                workspaceId,
                originalContent,
                newContent
        );

        verify(chatSessionService)
                .saveAssistantMessage(request, expectedAnswer);

        verifyNoInteractions(
                aiChatClient,
                knowledgeRetriever,
                knowledgeContextBuilder,
                memoryContextBuilder,
                chatPromptBuilder,
                workspaceService
        );

        verifyNoMoreInteractions(memoryService);
    }


    @Test
    void shouldRejectUpdateWhenMultipleMemoriesMatch() {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String originalContent = "I prefer Java.";
        String newContent = "I prefer Python.";

        String expectedAnswer =
                "Unable to update your memory. "
                        + "Multiple memories match the original content.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Update my memory from \"" + originalContent
                        + "\" to \"" + newContent + "\"."
        );

        UpdateMemoryCommand updateCommand =
                new UpdateMemoryCommand(
                        originalContent,
                        newContent
                );

        PreparedChat preparedChat =
                new PreparedChat(
                        userMessage,
                        List.of(userMessage)
                );

        when(userMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(assistantMessage.getId())
                .thenReturn(UUID.randomUUID());

        when(chatSessionService.prepareConversation(request))
                .thenReturn(preparedChat);

        when(memoryCommandDetector.extractUpdateCommand(
                request.message()
        )).thenReturn(Optional.of(updateCommand));

        when(memoryService.update(
                workspaceId,
                originalContent,
                newContent
        )).thenThrow(
                new IllegalStateException(
                        "Multiple memories match the original content."
                )
        );

        when(chatSessionService.saveAssistantMessage(
                request,
                expectedAnswer
        )).thenReturn(assistantMessage);

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenReturn(conversation);

        when(conversation.hasDefaultTitle())
                .thenReturn(false);

        ChatResponse response = chatService.chat(request);

        assertThat(response.answer())
                .isEqualTo(expectedAnswer);

        verify(memoryService).update(
                workspaceId,
                originalContent,
                newContent
        );

        verify(chatSessionService)
                .saveAssistantMessage(request, expectedAnswer);

        verifyNoInteractions(
                aiChatClient,
                knowledgeRetriever,
                knowledgeContextBuilder,
                memoryContextBuilder,
                chatPromptBuilder,
                workspaceService
        );

        verifyNoMoreInteractions(memoryService);
    }

}