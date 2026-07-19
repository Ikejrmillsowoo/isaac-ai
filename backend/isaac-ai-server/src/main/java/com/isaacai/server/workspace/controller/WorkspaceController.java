package com.isaacai.server.workspace.controller;

import com.isaacai.server.workspace.dto.CreateWorkspaceRequest;
import com.isaacai.server.workspace.dto.UpdateWorkspaceRequest;
import com.isaacai.server.workspace.dto.WorkspaceResponse;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(
            @Valid @RequestBody CreateWorkspaceRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        Workspace workspace = workspaceService.create(
                request.name(),
                request.description(),
                request.systemPrompt(),
                request.color()
        );

        URI location = uriBuilder
                .path("/api/workspaces/{id}")
                .buildAndExpand(workspace.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(WorkspaceResponse.from(workspace));
    }

    @GetMapping
    public List<WorkspaceResponse> findActive() {
        return workspaceService.findActive()
                .stream()
                .map(WorkspaceResponse::from)
                .toList();
    }

    @GetMapping("/archived")
    public List<WorkspaceResponse> findArchived() {
        return workspaceService.findArchived()
                .stream()
                .map(WorkspaceResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public WorkspaceResponse findById(@PathVariable UUID id) {
        Workspace workspace = workspaceService.findById(id);

        return WorkspaceResponse.from(workspace);
    }

    @PatchMapping("/{id}")
    public WorkspaceResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request
    ) {
        Workspace workspace = workspaceService.update(
                id,
                request.name(),
                request.description(),
                request.systemPrompt(),
                request.color()
        );

        return WorkspaceResponse.from(workspace);
    }

    @PostMapping("/{id}/archive")
    public WorkspaceResponse archive(@PathVariable UUID id) {
        Workspace workspace = workspaceService.archive(id);

        return WorkspaceResponse.from(workspace);
    }

    @PostMapping("/{id}/restore")
    public WorkspaceResponse restore(@PathVariable UUID id) {
        Workspace workspace = workspaceService.restore(id);

        return WorkspaceResponse.from(workspace);
    }
}