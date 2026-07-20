package com.isaacai.server.conversation.service;

import com.isaacai.server.conversation.exception.ConversationNotFoundException;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.repository.ConversationRepository;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final WorkspaceService workspaceService;

    public ConversationService(
            ConversationRepository conversationRepository,
            WorkspaceService workspaceService
    ) {
        this.conversationRepository = conversationRepository;
        this.workspaceService = workspaceService;
    }

    public Conversation create(
            UUID workspaceId,
            String title
    ) {
        Workspace workspace =
                workspaceService.findById(workspaceId);

        Conversation conversation =
                new Conversation(workspace, title);

        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Conversation findById(
            UUID workspaceId,
            UUID conversationId
    ) {
        return conversationRepository
                .findByIdAndWorkspaceId(
                        conversationId,
                        workspaceId
                )
                .orElseThrow(
                        () -> new ConversationNotFoundException(
                                conversationId
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Conversation> findActive(
            UUID workspaceId
    ) {
        workspaceService.findById(workspaceId);

        return conversationRepository
                .findAllByWorkspaceIdAndArchivedFalseOrderByPinnedDescUpdatedAtDesc(
                        workspaceId
                );
    }

    @Transactional(readOnly = true)
    public List<Conversation> findArchived(
            UUID workspaceId
    ) {
        workspaceService.findById(workspaceId);

        return conversationRepository
                .findAllByWorkspaceIdAndArchivedTrueOrderByUpdatedAtDesc(
                        workspaceId
                );
    }

    public Conversation rename(
            UUID workspaceId,
            UUID conversationId,
            String title
    ) {
        Conversation conversation =
                findById(workspaceId, conversationId);

        conversation.rename(title);

        return conversation;
    }

    public Conversation pin(
            UUID workspaceId,
            UUID conversationId
    ) {
        Conversation conversation =
                findById(workspaceId, conversationId);

        conversation.pin();

        return conversation;
    }

    public Conversation unpin(
            UUID workspaceId,
            UUID conversationId
    ) {
        Conversation conversation =
                findById(workspaceId, conversationId);

        conversation.unpin();

        return conversation;
    }

    public Conversation archive(
            UUID workspaceId,
            UUID conversationId
    ) {
        Conversation conversation =
                findById(workspaceId, conversationId);

        conversation.archive();

        return conversation;
    }

    public Conversation restore(
            UUID workspaceId,
            UUID conversationId
    ) {
        Conversation conversation =
                findById(workspaceId, conversationId);

        conversation.restore();

        return conversation;
    }

    public Conversation update(
        UUID workspaceId,
        UUID conversationId,
        String title
) {
    Conversation conversation =
            findById(workspaceId, conversationId);

    if (title != null) {
        conversation.rename(title);
    }

    return conversation;
}
}
