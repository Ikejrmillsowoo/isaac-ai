package com.isaacai.server.conversation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaacai.server.conversation.exception.ConversationNotFoundException;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.workspace.model.Workspace;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversationController.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConversationService conversationService;

    @Test
    void shouldCreateConversation() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Spring architecture"
        );

        when(conversationService.create(
                workspace.getId(),
                "Spring architecture"
        )).thenReturn(conversation);

        String requestBody = """
                {
                  "title": "Spring architecture"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations",
                                workspace.getId()
                        )
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(conversation.getId().toString()))
                .andExpect(jsonPath("$.workspaceId")
                        .value(workspace.getId().toString()))
                .andExpect(jsonPath("$.title")
                        .value("Spring architecture"))
                .andExpect(jsonPath("$.pinned").value(false))
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    void shouldRejectCreateRequestWithBlankTitle() throws Exception {
        Workspace workspace = createWorkspace();

        String requestBody = """
                {
                  "title": "   "
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/workspaces/{workspaceId}/conversations",
                                workspace.getId()
                        )
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title")
                        .value("Conversation title is required."));
    }

    @Test
    void shouldReturnActiveConversations() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation first = new Conversation(
                workspace,
                "Spring architecture"
        );

        Conversation second = new Conversation(
                workspace,
                "React workspace UI"
        );

        second.pin();

        when(conversationService.findActive(workspace.getId()))
                .thenReturn(List.of(second, first));

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations",
                                workspace.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title")
                        .value("React workspace UI"))
                .andExpect(jsonPath("$[0].pinned").value(true))
                .andExpect(jsonPath("$[1].title")
                        .value("Spring architecture"));
    }

    @Test
    void shouldReturnArchivedConversations() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Old research"
        );

        conversation.archive();

        when(conversationService.findArchived(workspace.getId()))
                .thenReturn(List.of(conversation));

        mockMvc.perform(
                        get(
                                "/api/workspaces/{workspaceId}/conversations/archived",
                                workspace.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title")
                        .value("Old research"))
                .andExpect(jsonPath("$[0].archived").value(true));
    }

    @Test
    void shouldReturnConversationById() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Spring architecture"
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
                .andExpect(jsonPath("$.title")
                        .value("Spring architecture"));
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
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Conversation with ID '"
                                        + conversationId
                                        + "' was not found."
                        ));
    }

    @Test
    void shouldUpdateConversationTitle() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Old title"
        );

        conversation.rename("New title");

        when(conversationService.update(
                workspace.getId(),
                conversation.getId(),
                "New title"
        )).thenReturn(conversation);

        String requestBody = """
                {
                  "title": "New title"
                }
                """;

        mockMvc.perform(
                        patch(
                                "/api/workspaces/{workspaceId}/conversations/{conversationId}",
                                workspace.getId(),
                                conversation.getId()
                        )
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("New title"));
    }

    @Test
    void shouldPinConversation() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Research"
        );

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
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void shouldUnpinConversation() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Research"
        );

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
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void shouldArchiveConversation() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Research"
        );

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
                .andExpect(jsonPath("$.archived").value(true));
    }

    @Test
    void shouldRestoreConversation() throws Exception {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Research"
        );

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
                .andExpect(jsonPath("$.archived").value(false));
    }

    private Workspace createWorkspace() {
        return new Workspace(
                "Software",
                null,
                null,
                null
        );
    }
}