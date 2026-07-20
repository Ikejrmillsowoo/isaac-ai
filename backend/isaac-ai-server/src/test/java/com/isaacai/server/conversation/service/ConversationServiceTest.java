package com.isaacai.server.conversation.service;

import com.isaacai.server.conversation.exception.ConversationNotFoundException;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.repository.ConversationRepository;
import com.isaacai.server.workspace.exception.WorkspaceNotFoundException;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private WorkspaceService workspaceService;

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(
                conversationRepository,
                workspaceService
        );
    }

    @Test
    void shouldCreateConversationWhenWorkspaceExists() {
        Workspace workspace = createWorkspace();
        UUID workspaceId = workspace.getId();

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Conversation conversation = conversationService.create(
                workspaceId,
                "  Spring architecture  "
        );

        assertThat(conversation.getWorkspace()).isSameAs(workspace);
        assertThat(conversation.getTitle())
                .isEqualTo("Spring architecture");
        assertThat(conversation.isPinned()).isFalse();
        assertThat(conversation.isArchived()).isFalse();

        verify(workspaceService).findById(workspaceId);
        verify(conversationRepository)
                .save(any(Conversation.class));
    }

    @Test
void shouldNotCreateConversationWhenWorkspaceDoesNotExist() {
    UUID workspaceId = UUID.randomUUID();

    WorkspaceNotFoundException exception =
            new WorkspaceNotFoundException(workspaceId);

    when(workspaceService.findById(workspaceId))
            .thenThrow(exception);

    assertThatThrownBy(() ->
            conversationService.create(
                    workspaceId,
                    "Spring architecture"
            )
    )
            .isSameAs(exception);

    verify(conversationRepository, never())
            .save(any(Conversation.class));
}

    @Test
    void shouldFindConversationByIdAndWorkspaceId() {
        Workspace workspace = createWorkspace();
        Conversation conversation = new Conversation(
                workspace,
                "Spring architecture"
        );

        UUID workspaceId = workspace.getId();
        UUID conversationId = conversation.getId();

        when(conversationRepository.findByIdAndWorkspaceId(
                conversationId,
                workspaceId
        )).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.findById(
                workspaceId,
                conversationId
        );

        assertThat(result).isSameAs(conversation);
    }

    @Test
    void shouldThrowExceptionWhenConversationDoesNotExist() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        when(conversationRepository.findByIdAndWorkspaceId(
                conversationId,
                workspaceId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                conversationService.findById(
                        workspaceId,
                        conversationId
                )
        )
                .isInstanceOf(
                        ConversationNotFoundException.class
                )
                .hasMessage(
                        "Conversation with ID '"
                                + conversationId
                                + "' was not found."
                );
    }

    @Test
    void shouldReturnActiveConversationsForWorkspace() {
        Workspace workspace = createWorkspace();
        UUID workspaceId = workspace.getId();

        Conversation first = new Conversation(
                workspace,
                "Spring architecture"
        );

        Conversation second = new Conversation(
                workspace,
                "React workspace UI"
        );

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        when(conversationRepository
                .findAllByWorkspaceIdAndArchivedFalseOrderByPinnedDescUpdatedAtDesc(
                        workspaceId
                ))
                .thenReturn(List.of(first, second));

        List<Conversation> results =
                conversationService.findActive(workspaceId);

        assertThat(results).containsExactly(first, second);

        verify(workspaceService).findById(workspaceId);

        verify(conversationRepository)
                .findAllByWorkspaceIdAndArchivedFalseOrderByPinnedDescUpdatedAtDesc(
                        workspaceId
                );
    }

    @Test
    void shouldReturnArchivedConversationsForWorkspace() {
        Workspace workspace = createWorkspace();
        UUID workspaceId = workspace.getId();

        Conversation archived = new Conversation(
                workspace,
                "Old research"
        );

        archived.archive();

        when(workspaceService.findById(workspaceId))
                .thenReturn(workspace);

        when(conversationRepository
                .findAllByWorkspaceIdAndArchivedTrueOrderByUpdatedAtDesc(
                        workspaceId
                ))
                .thenReturn(List.of(archived));

        List<Conversation> results =
                conversationService.findArchived(workspaceId);

        assertThat(results).containsExactly(archived);

        verify(workspaceService).findById(workspaceId);

        verify(conversationRepository)
                .findAllByWorkspaceIdAndArchivedTrueOrderByUpdatedAtDesc(
                        workspaceId
                );
    }

    @Test
    void shouldRenameConversation() {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Old title"
        );

        UUID workspaceId = workspace.getId();
        UUID conversationId = conversation.getId();

        when(conversationRepository.findByIdAndWorkspaceId(
                conversationId,
                workspaceId
        )).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.rename(
                workspaceId,
                conversationId,
                "  New title  "
        );

        assertThat(result).isSameAs(conversation);
        assertThat(result.getTitle()).isEqualTo("New title");
    }

    @Test
    void shouldPinConversation() {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Research"
        );

        UUID workspaceId = workspace.getId();
        UUID conversationId = conversation.getId();

        when(conversationRepository.findByIdAndWorkspaceId(
                conversationId,
                workspaceId
        )).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.pin(
                workspaceId,
                conversationId
        );

        assertThat(result.isPinned()).isTrue();
    }

    @Test
    void shouldUnpinConversation() {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Research"
        );

        conversation.pin();

        UUID workspaceId = workspace.getId();
        UUID conversationId = conversation.getId();

        when(conversationRepository.findByIdAndWorkspaceId(
                conversationId,
                workspaceId
        )).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.unpin(
                workspaceId,
                conversationId
        );

        assertThat(result.isPinned()).isFalse();
    }

    @Test
    void shouldArchiveConversation() {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Research"
        );

        UUID workspaceId = workspace.getId();
        UUID conversationId = conversation.getId();

        when(conversationRepository.findByIdAndWorkspaceId(
                conversationId,
                workspaceId
        )).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.archive(
                workspaceId,
                conversationId
        );

        assertThat(result.isArchived()).isTrue();
    }

    @Test
    void shouldRestoreConversation() {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Research"
        );

        conversation.archive();

        UUID workspaceId = workspace.getId();
        UUID conversationId = conversation.getId();

        when(conversationRepository.findByIdAndWorkspaceId(
                conversationId,
                workspaceId
        )).thenReturn(Optional.of(conversation));

        Conversation result = conversationService.restore(
                workspaceId,
                conversationId
        );

        assertThat(result.isArchived()).isFalse();
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