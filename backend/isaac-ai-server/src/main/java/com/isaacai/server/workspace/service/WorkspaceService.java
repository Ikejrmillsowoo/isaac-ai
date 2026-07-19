package com.isaacai.server.workspace.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isaacai.server.workspace.exception.WorkspaceAlreadyExistsException;
import com.isaacai.server.workspace.exception.WorkspaceNotFoundException;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.repository.WorkspaceRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public Workspace create(
            String name,
            String description,
            String systemPrompt,
            String color
    ) {
        String normalizedName = normalizeName(name);

        if (workspaceRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new WorkspaceAlreadyExistsException(normalizedName);
        }

        Workspace workspace = new Workspace(
                normalizedName,
                description,
                systemPrompt,
                color
        );

        return workspaceRepository.save(workspace);
    }

    @Transactional(readOnly = true)
    public Workspace findById(UUID id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Workspace> findActive() {
        return workspaceRepository
                .findAllByArchivedFalseOrderByCreatedAtAsc();
    }

    @Transactional(readOnly = true)
    public List<Workspace> findArchived() {
        return workspaceRepository
                .findAllByArchivedTrueOrderByCreatedAtAsc();
    }

    public Workspace rename(UUID id, String newName) {
        Workspace workspace = findById(id);
        String normalizedName = normalizeName(newName);

        if (workspaceRepository
            .existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
        throw new WorkspaceAlreadyExistsException(normalizedName);
    }

        workspace.rename(normalizedName);

        return workspace;
    }

    public Workspace updateDetails(
            UUID id,
            String description,
            String systemPrompt,
            String color
    ) {
        Workspace workspace = findById(id);

        workspace.updateDescription(description);
        workspace.updateSystemPrompt(systemPrompt);
        workspace.updateColor(color);

        return workspace;
    }

    public Workspace archive(UUID id) {
        Workspace workspace = findById(id);
        workspace.archive();

        return workspace;
    }

    public Workspace restore(UUID id) {
        Workspace workspace = findById(id);
        workspace.restore();

        return workspace;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Workspace name must not be blank"
            );
        }

        return name.trim();
    }


public Workspace update(
        UUID id,
        String name,
        String description,
        String systemPrompt,
        String color
) {
    Workspace workspace = findById(id);

    if (name != null) {
        String normalizedName = normalizeName(name);

        if (workspaceRepository
                .existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new WorkspaceAlreadyExistsException(normalizedName);
        }

        workspace.rename(normalizedName);
    }

    if (description != null) {
        workspace.updateDescription(description);
    }

    if (systemPrompt != null) {
        workspace.updateSystemPrompt(systemPrompt);
    }

    if (color != null) {
        workspace.updateColor(color);
    }

    return workspace;
}
    
}