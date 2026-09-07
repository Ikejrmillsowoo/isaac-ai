package com.isaacai.server.memory.service;

import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.model.MemoryCategory;
import com.isaacai.server.memory.repository.MemoryRepository;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MemoryService {

    private final MemoryRepository memoryRepository;
    private final WorkspaceService workspaceService;

    public MemoryService(
            MemoryRepository memoryRepository,
            WorkspaceService workspaceService
    ) {
        this.memoryRepository = memoryRepository;
        this.workspaceService = workspaceService;
    }

    public Memory create(
            UUID workspaceId,
            String content,
            MemoryCategory category,
            UUID sourceConversationId
    ) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Memory content is required."
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "Memory category is required."
            );
        }

        Workspace workspace =
                workspaceService.findById(workspaceId);

        Memory memory =
                new Memory(
                        workspace,
                        content.trim(),
                        category,
                        sourceConversationId
                );

        return memoryRepository.save(memory);
    }

    @Transactional(readOnly = true)
    public List<Memory> findAll(
            UUID workspaceId
    ) {
        workspaceService.findById(workspaceId);

        return memoryRepository
                .findAllByWorkspaceIdOrderByCreatedAtDesc(
                        workspaceId
                );
    }

    public void delete(
            UUID workspaceId,
            UUID memoryId
    ) {
        workspaceService.findById(workspaceId);

        Memory memory =
                memoryRepository.findById(memoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Memory not found."
                                )
                        );

        if (!memory.getWorkspace()
                .getId()
                .equals(workspaceId)) {

            throw new IllegalArgumentException(
                    "Memory does not belong to this workspace."
            );
        }

        memoryRepository.delete(memory);
    }
}