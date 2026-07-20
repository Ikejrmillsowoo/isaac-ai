package com.isaacai.server.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaacai.server.common.exception.GlobalExceptionHandler;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.message.exception.MessageNotFoundException;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import com.isaacai.server.message.service.MessageService;
import com.isaacai.server.workspace.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.isaacai.support.TestDataFactory.conversation;
import static com.isaacai.support.TestDataFactory.userMessage;
import static com.isaacai.support.TestDataFactory.workspace;

import static com.isaacai.support.TestDataFactory.assistantMessage;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MessageController messageController =
                new MessageController(messageService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(messageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateUserMessage() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);
        Message message = userMessage(conversation, "Explain dependency injection.");

        when(messageService.createUserMessage(
                workspace.getId(),
                conversation.getId(),
                "Explain dependency injection."
        )).thenReturn(message);

        String requestBody = """
                {
                  "content": "Explain dependency injection."
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages",
                                workspace.getId(),
                                conversation.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(message.getId().toString()))
                .andExpect(jsonPath("$.conversationId")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.content")
                        .value("Explain dependency injection."));

        verify(messageService).createUserMessage(
                workspace.getId(),
                conversation.getId(),
                "Explain dependency injection."
        );
    }

    @Test
    void shouldRejectCreateRequestWhenContentIsMissing()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String requestBody = """
                {
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages",
                                workspaceId,
                                conversationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateRequestWhenContentIsBlank()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String requestBody = """
                {
                  "content": "   "
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages",
                                workspaceId,
                                conversationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateRequestWhenContentExceedsLimit()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String requestBody = objectMapper.writeValueAsString(
                new ContentRequest("a".repeat(20_001))
        );

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages",
                                workspaceId,
                                conversationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldIgnoreRoleSubmittedByClient() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        Message message = userMessage(conversation,"Client attempted to set a role.");

        when(messageService.createUserMessage(
                workspace.getId(),
                conversation.getId(),
                "Client attempted to set a role."
        )).thenReturn(message);

        String requestBody = """
                {
                  "role": "ASSISTANT",
                  "content": "Client attempted to set a role."
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages",
                                workspace.getId(),
                                conversation.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));

        verify(messageService).createUserMessage(
                workspace.getId(),
                conversation.getId(),
                "Client attempted to set a role."
        );
    }

    @Test
    void shouldReturnConversationMessages() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        Message first = userMessage(conversation, "Question");

        Message second = assistantMessage(
                conversation,
                "Answer"
        );

        when(messageService.findConversationMessages(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(List.of(first, second));

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages",
                                workspace.getId(),
                                conversation.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id")
                        .value(first.getId().toString()))
                .andExpect(jsonPath("$[0].role")
                        .value("USER"))
                .andExpect(jsonPath("$[0].content")
                        .value("Question"))
                .andExpect(jsonPath("$[1].id")
                        .value(second.getId().toString()))
                .andExpect(jsonPath("$[1].role")
                        .value("ASSISTANT"))
                .andExpect(jsonPath("$[1].content")
                        .value("Answer"));
    }

    @Test
    void shouldReturnEmptyListWhenConversationHasNoMessages()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        when(messageService.findConversationMessages(
                workspaceId,
                conversationId
        )).thenReturn(List.of());

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages",
                                workspaceId,
                                conversationId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnMessageById() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        Message message = userMessage(conversation, "Hello");

        when(messageService.findById(
                workspace.getId(),
                conversation.getId(),
                message.getId()
        )).thenReturn(message);

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages/{messageId}",
                                workspace.getId(),
                                conversation.getId(),
                                message.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(message.getId().toString()))
                .andExpect(jsonPath("$.conversationId")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void shouldReturnNotFoundWhenMessageDoesNotExist()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        when(messageService.findById(
                workspaceId,
                conversationId,
                messageId
        )).thenThrow(new MessageNotFoundException(messageId));

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages/{messageId}",
                                workspaceId,
                                conversationId,
                                messageId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Message with ID '" + messageId
                                        + "' was not found."
                        ));
    }

    @Test
    void shouldUpdateMessage() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        Message message = userMessage(conversation, "Original content");

        message.updateContent("Updated content");

        when(messageService.update(
                workspace.getId(),
                conversation.getId(),
                message.getId(),
                "Updated content"
        )).thenReturn(message);

        String requestBody = """
                {
                  "content": "Updated content"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages/{messageId}",
                                workspace.getId(),
                                conversation.getId(),
                                message.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(message.getId().toString()))
                .andExpect(jsonPath("$.content")
                        .value("Updated content"));

        verify(messageService).update(
                workspace.getId(),
                conversation.getId(),
                message.getId(),
                "Updated content"
        );
    }

    @Test
    void shouldRejectUpdateRequestWhenContentIsMissing()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        mockMvc.perform(
                        put(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages/{messageId}",
                                workspaceId,
                                conversationId,
                                messageId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdateRequestWhenContentIsBlank()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        String requestBody = """
                {
                  "content": "   "
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages/{messageId}",
                                workspaceId,
                                conversationId,
                                messageId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteMessage() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        doNothing().when(messageService).delete(
                workspaceId,
                conversationId,
                messageId
        );

        mockMvc.perform(
                        delete(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages/{messageId}",
                                workspaceId,
                                conversationId,
                                messageId
                        )
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(messageService).delete(
                workspaceId,
                conversationId,
                messageId
        );
    }


    private record ContentRequest(String content) {
    }
}