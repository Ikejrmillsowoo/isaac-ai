package com.isaacai.server.workspace.service;

import com.isaacai.server.workspace.exception.WorkspaceAlreadyExistsException;
import com.isaacai.server.workspace.exception.WorkspaceNotFoundException;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    private WorkspaceService workspaceService;

    @BeforeEach
    void setUp() {
        workspaceService = new WorkspaceService(workspaceRepository);
    }

    @Test
    void shouldCreateWorkspaceWhenNameIsAvailable() {

        when(workspaceRepository.existsByNameIgnoreCase("Software"))
                .thenReturn(false);

        when(workspaceRepository.save(any(Workspace.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Workspace workspace = workspaceService.create(
                " Software ",
                "Engineering projects",
                "Act as a senior engineer.",
                "#2563EB"
        );

        assertThat(workspace.getName()).isEqualTo("Software");
        assertThat(workspace.getDescription())
                .isEqualTo("Engineering projects");
        assertThat(workspace.getSystemPrompt())
                .isEqualTo("Act as a senior engineer.");
        assertThat(workspace.getColor()).isEqualTo("#2563EB");
        assertThat(workspace.isArchived()).isFalse();

        verify(workspaceRepository)
                .existsByNameIgnoreCase("Software");

        verify(workspaceRepository)
                .save(any(Workspace.class));
    }

    @Test
    void shouldRejectDuplicateWorkspaceName() {

        when(workspaceRepository.existsByNameIgnoreCase("Software"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                workspaceService.create(
                        "Software",
                        null,
                        null,
                        null
                ))
                .isInstanceOf(WorkspaceAlreadyExistsException.class)
                .hasMessage("Workspace 'Software' already exists");

        verify(workspaceRepository, never())
                .save(any());
    }

    @Test
    void shouldFindWorkspaceById() {

        Workspace workspace = new Workspace(
                "Research",
                null,
                null,
                null
        );

        UUID id = workspace.getId();

        when(workspaceRepository.findById(id))
                .thenReturn(Optional.of(workspace));

        Workspace result = workspaceService.findById(id);

        assertThat(result).isSameAs(workspace);
    }

    @Test
    void shouldThrowExceptionWhenWorkspaceDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(workspaceRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.findById(id))
                .isInstanceOf(WorkspaceNotFoundException.class)
                .hasMessage("Workspace with ID '" + id + "' was not found.");
    }

    @Test
    void shouldArchiveWorkspace() {

        Workspace workspace = new Workspace(
                "Church",
                null,
                null,
                null
        );

        UUID id = workspace.getId();

        when(workspaceRepository.findById(id))
                .thenReturn(Optional.of(workspace));

        Workspace result = workspaceService.archive(id);

        assertThat(result.isArchived()).isTrue();
    }

    @Test
    void shouldRestoreWorkspace() {

        Workspace workspace = new Workspace(
                "Church",
                null,
                null,
                null
        );

        workspace.archive();

        UUID id = workspace.getId();

        when(workspaceRepository.findById(id))
                .thenReturn(Optional.of(workspace));

        Workspace result = workspaceService.restore(id);

        assertThat(result.isArchived()).isFalse();
    }

    @Test
    void shouldRenameWorkspaceWhenNameIsAvailable() {

        Workspace workspace = new Workspace(
                "Software",
                null,
                null,
                null
        );

        UUID id = workspace.getId();

        when(workspaceRepository.findById(id))
                .thenReturn(Optional.of(workspace));

        when(workspaceRepository.existsByNameIgnoreCaseAndIdNot(
                "Healthcare",
                id))
                .thenReturn(false);

        Workspace result = workspaceService.rename(
                id,
                " Healthcare "
        );

        assertThat(result.getName()).isEqualTo("Healthcare");
    }

    @Test
    void shouldRejectRenameWhenNameAlreadyExists() {

        Workspace workspace = new Workspace(
                "Software",
                null,
                null,
                null
        );

        UUID id = workspace.getId();

        when(workspaceRepository.findById(id))
                .thenReturn(Optional.of(workspace));

        when(workspaceRepository.existsByNameIgnoreCaseAndIdNot(
                "Research",
                id))
                .thenReturn(true);

        assertThatThrownBy(() ->
                workspaceService.rename(id, "Research"))
                .isInstanceOf(WorkspaceAlreadyExistsException.class)
                .hasMessage("Workspace 'Research' already exists");

        assertThat(workspace.getName()).isEqualTo("Software");
    }

   
}