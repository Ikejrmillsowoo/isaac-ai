package com.isaacai.tools;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.isaacai.server.workspace.service.WorkspaceService;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/tools")
public class ToolController {
    private final TaskActions actions;
    private final WorkspaceService workspaces;
    public ToolController(TaskActions actions, WorkspaceService workspaces) {
        this.actions=actions; this.workspaces=workspaces;
    }
    @GetMapping("/permission")
    public Map<String,Boolean> permission(@PathVariable UUID workspaceId) {
        workspaces.findById(workspaceId);
        return Map.of("enabled",actions.enabled(workspaceId));
    }
    @PutMapping("/permission")
    public Map<String,Boolean> permission(@PathVariable UUID workspaceId,@RequestBody Permission permission) {
        workspaces.findById(workspaceId);
        if(permission.enabled()==null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Permission required");
        actions.setEnabled(workspaceId,permission.enabled());
        return Map.of("enabled",permission.enabled());
    }
    public record Permission(Boolean enabled) {}
    @GetMapping("/tasks")
    public List<Map<String,Object>> tasks(@PathVariable UUID workspaceId) {
        workspaces.findById(workspaceId);
        return actions.tasks(workspaceId);
    }
    @GetMapping("/history")
    public List<Map<String,Object>> history(@PathVariable UUID workspaceId) {
        workspaces.findById(workspaceId);
        return actions.history(workspaceId);
    }
    @PostMapping("/history/{id}/decision")
    public Map<String,String> decide(@PathVariable UUID workspaceId,@PathVariable UUID id,@RequestBody Decision decision) {
        workspaces.findById(workspaceId);
        if(decision.approve()==null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Decision required");
        return Map.of("result",actions.decide(workspaceId,id,decision.approve()));
    }
    public record Decision(Boolean approve) {}
}
