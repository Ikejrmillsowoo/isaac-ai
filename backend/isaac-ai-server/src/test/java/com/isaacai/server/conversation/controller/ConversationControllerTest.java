package com.isaacai.server.conversation.controller;

import com.isaacai.server.common.exception.GlobalExceptionHandler;
import com.isaacai.server.conversation.exception.ConversationNotFoundException;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.workspace.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static com.isaacai.support.TestDataFactory.conversation;
import static com.isaacai.support.TestDataFactory.workspace;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ConversationController controller =
                new ConversationController(conversationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateConversation() throws Exception {
        Workspace workspace = workspace();

        Conversation conversation = conversation(
                workspace,
                "Spring Boot"
        );

        when(conversationService.create(
                workspace.getId(),
                "Spring Boot"
        )).thenReturn(conversation);

        String requestBody = """
                {
                  "title": "Spring Boot"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations",
                                workspace.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.workspaceId")
                        .value(workspace.getId().toString()))
                .andExpect(jsonPath("$.title")
                        .value("Spring Boot"))
                .andExpect(jsonPath("$.pinned")
                        .value(false))
                .andExpect(jsonPath("$.archived")
                        .value(false));

        verify(conversationService).create(
                workspace.getId(),
                "Spring Boot"
        );
    }

    @Test
    void shouldRejectCreateRequestWhenTitleIsMissing()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations",
                                workspaceId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateRequestWhenTitleIsBlank()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();

        String requestBody = """
                {
                  "title": "   "
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations",
                                workspaceId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnActiveConversations() throws Exception {
        Workspace workspace = workspace();

        Conversation first = conversation(
                workspace,
                "First conversation"
        );

        Conversation second = conversation(
                workspace,
                "Second conversation"
        );

        second.pin();

        when(conversationService.findActive(
                workspace.getId()
        )).thenReturn(List.of(second, first));

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations",
                                workspace.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id")
                        .value(second.getId().toString()))
                .andExpect(jsonPath("$[0].title")
                        .value("Second conversation"))
                .andExpect(jsonPath("$[0].pinned")
                        .value(true))
                .andExpect(jsonPath("$[0].archived")
                        .value(false))
                .andExpect(jsonPath("$[1].id")
                        .value(first.getId().toString()))
                .andExpect(jsonPath("$[1].title")
                        .value("First conversation"));
    }

    @Test
    void shouldReturnEmptyListWhenWorkspaceHasNoActiveConversations()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();

        when(conversationService.findActive(
                workspaceId
        )).thenReturn(List.of());

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations",
                                workspaceId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnArchivedConversations() throws Exception {
        Workspace workspace = workspace();

        Conversation first = conversation(
                workspace,
                "Archived conversation"
        );

        first.archive();

        when(conversationService.findArchived(
                workspace.getId()
        )).thenReturn(List.of(first));

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations/archived",
                                workspace.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(first.getId().toString()))
                .andExpect(jsonPath("$[0].title")
                        .value("Archived conversation"))
                .andExpect(jsonPath("$[0].archived")
                        .value(true));
    }

    @Test
    void shouldReturnConversationById() throws Exception {
        Workspace workspace = workspace();

        Conversation conversation = conversation(
                workspace,
                "Spring Boot"
        );

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}",
                                workspace.getId(),
                                conversation.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.workspaceId")
                        .value(workspace.getId().toString()))
                .andExpect(jsonPath("$.title")
                        .value("Spring Boot"))
                .andExpect(jsonPath("$.pinned")
                        .value(false))
                .andExpect(jsonPath("$.archived")
                        .value(false));
    }

    @Test
    void shouldReturnNotFoundWhenConversationDoesNotExist()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenThrow(
                new ConversationNotFoundException(conversationId)
        );

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}",
                                workspaceId,
                                conversationId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Conversation with ID '"
                                        + conversationId
                                        + "' was not found."
                        ));
    }

   @Test
void shouldUpdateConversation() throws Exception {
    Workspace workspace = workspace();

    Conversation conversation = conversation(
            workspace,
            "Original title"
    );

    conversation.rename("Updated title");

    when(conversationService.update(
            workspace.getId(),
            conversation.getId(),
            "Updated title"
    )).thenReturn(conversation);

    String requestBody = """
            {
              "title": "Updated title"
            }
            """;

    mockMvc.perform(
                    patch(
                            "/api/workspaces/{workspaceId}/conversations/{conversationId}",
                            workspace.getId(),
                            conversation.getId()
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id")
                    .value(conversation.getId().toString()))
            .andExpect(jsonPath("$.title")
                    .value("Updated title"));

    verify(conversationService).update(
            workspace.getId(),
            conversation.getId(),
            "Updated title"
    );
}

    @Test
    void shouldRejectUpdateRequestWhenTitleIsMissing()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}",
                                workspaceId,
                                conversationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdateRequestWhenTitleIsBlank()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        String requestBody = """
                {
                  "title": "   "
                }
                """;

        mockMvc.perform(
                        patch(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}",
                                workspaceId,
                                conversationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPinConversation() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        conversation.pin();

        when(conversationService.pin(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/pin",
                                workspace.getId(),
                                conversation.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.pinned")
                        .value(true));

        verify(conversationService).pin(
                workspace.getId(),
                conversation.getId()
        );
    }

    @Test
    void shouldUnpinConversation() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        when(conversationService.unpin(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/unpin",
                                workspace.getId(),
                                conversation.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.pinned")
                        .value(false));

        verify(conversationService).unpin(
                workspace.getId(),
                conversation.getId()
        );
    }

    @Test
    void shouldArchiveConversation() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        conversation.archive();

        when(conversationService.archive(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/archive",
                                workspace.getId(),
                                conversation.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.archived")
                        .value(true));

        verify(conversationService).archive(
                workspace.getId(),
                conversation.getId()
        );
    }

    @Test
    void shouldRestoreConversation() throws Exception {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        when(conversationService.restore(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}/restore",
                                workspace.getId(),
                                conversation.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.archived")
                        .value(false));

        verify(conversationService).restore(
                workspace.getId(),
                conversation.getId()
        );
    }
}