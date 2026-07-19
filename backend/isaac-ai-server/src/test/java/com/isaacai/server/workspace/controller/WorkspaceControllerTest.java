package com.isaacai.server.workspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaacai.server.common.exception.GlobalExceptionHandler;
import com.isaacai.server.workspace.exception.WorkspaceNotFoundException;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkspaceController.class)
@Import(GlobalExceptionHandler.class)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkspaceService workspaceService;

    @Test
    void shouldCreateWorkspace() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-07-18T12:00:00Z");

        Workspace workspace = mockWorkspace(
                workspaceId,
                "Software",
                "Software and AI projects",
                "Act as a senior software engineer.",
                "#2563EB",
                false,
                createdAt,
                createdAt
        );

        when(workspaceService.create(
                "Software",
                "Software and AI projects",
                "Act as a senior software engineer.",
                "#2563EB"
        )).thenReturn(workspace);

        String requestBody = """
                {
                  "name": "Software",
                  "description": "Software and AI projects",
                  "systemPrompt": "Act as a senior software engineer.",
                  "color": "#2563EB"
                }
                """;

        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/workspaces/" + workspaceId
                ))
                .andExpect(jsonPath("$.id").value(workspaceId.toString()))
                .andExpect(jsonPath("$.name").value("Software"))
                .andExpect(jsonPath("$.description")
                        .value("Software and AI projects"))
                .andExpect(jsonPath("$.systemPrompt")
                        .value("Act as a senior software engineer."))
                .andExpect(jsonPath("$.color").value("#2563EB"))
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    void shouldReturnActiveWorkspaces() throws Exception {
        Workspace software = mockWorkspace(
                UUID.randomUUID(),
                "Software",
                "Software projects",
                null,
                "#2563EB",
                false,
                Instant.parse("2026-07-18T12:00:00Z"),
                Instant.parse("2026-07-18T12:00:00Z")
        );

        Workspace writing = mockWorkspace(
                UUID.randomUUID(),
                "Writing",
                "Books and articles",
                null,
                "#7C3AED",
                false,
                Instant.parse("2026-07-18T13:00:00Z"),
                Instant.parse("2026-07-18T13:00:00Z")
        );

        when(workspaceService.findActive())
                .thenReturn(List.of(software, writing));

        mockMvc.perform(get("/api/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Software"))
                .andExpect(jsonPath("$[1].name").value("Writing"));
    }

    @Test
    void shouldReturnWorkspaceById() throws Exception {
        UUID workspaceId = UUID.randomUUID();

        Workspace workspace = mockWorkspace(
                workspaceId,
                "Software",
                "Software projects",
                null,
                "#2563EB",
                false,
                Instant.parse("2026-07-18T12:00:00Z"),
                Instant.parse("2026-07-18T12:00:00Z")
        );

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        mockMvc.perform(get("/api/workspaces/{id}", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workspaceId.toString()))
                .andExpect(jsonPath("$.name").value("Software"));
    }

    @Test
    void shouldUpdateWorkspace() throws Exception {
        UUID workspaceId = UUID.randomUUID();

        Workspace updatedWorkspace = mockWorkspace(
                workspaceId,
                "Software Development",
                "Software projects",
                null,
                "#7C3AED",
                false,
                Instant.parse("2026-07-18T12:00:00Z"),
                Instant.parse("2026-07-18T14:00:00Z")
        );

        when(workspaceService.update(
                workspaceId,
                "Software Development",
                null,
                null,
                "#7C3AED"
        )).thenReturn(updatedWorkspace);

        String requestBody = """
                {
                  "name": "Software Development",
                  "color": "#7C3AED"
                }
                """;

        mockMvc.perform(patch("/api/workspaces/{id}", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Software Development"))
                .andExpect(jsonPath("$.color").value("#7C3AED"));

        verify(workspaceService).update(
                workspaceId,
                "Software Development",
                null,
                null,
                "#7C3AED"
        );
    }

    @Test
    void shouldRejectCreateRequestWhenNameIsBlank() throws Exception {
        String requestBody = """
                {
                  "name": "   ",
                  "description": "Invalid workspace"
                }
                """;

        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(workspaceService, never()).create(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldReturnNotFoundWhenWorkspaceDoesNotExist() throws Exception {
        UUID workspaceId = UUID.randomUUID();

        when(workspaceService.findById(workspaceId))
                .thenThrow(new WorkspaceNotFoundException(workspaceId));

        mockMvc.perform(get("/api/workspaces/{id}", workspaceId))
                .andExpect(status().isNotFound());
    }

    private Workspace mockWorkspace(
            UUID id,
            String name,
            String description,
            String systemPrompt,
            String color,
            boolean archived,
            Instant createdAt,
            Instant updatedAt
    ) {
        Workspace workspace = org.mockito.Mockito.mock(Workspace.class);

        when(workspace.getId()).thenReturn(id);
        when(workspace.getName()).thenReturn(name);
        when(workspace.getDescription()).thenReturn(description);
        when(workspace.getSystemPrompt()).thenReturn(systemPrompt);
        when(workspace.getColor()).thenReturn(color);
        when(workspace.isArchived()).thenReturn(archived);
        when(workspace.getCreatedAt()).thenReturn(createdAt);
        when(workspace.getUpdatedAt()).thenReturn(updatedAt);

        return workspace;
    }
}