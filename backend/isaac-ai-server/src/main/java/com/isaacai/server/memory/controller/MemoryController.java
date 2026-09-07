package com.isaacai.server.memory.controller;

import com.isaacai.server.memory.dto.MemoryCreateRequest;
import com.isaacai.server.memory.dto.MemoryResponse;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.service.MemoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/memories")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(
            MemoryService memoryService
    ) {
        this.memoryService = memoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemoryResponse create(
            @PathVariable UUID workspaceId,
            @RequestBody MemoryCreateRequest request
    ) {
        Memory memory =
                memoryService.create(
                        workspaceId,
                        request.content(),
                        request.category(),
                        null
                );

        return MemoryResponse.from(memory);
    }

    @GetMapping
    public List<MemoryResponse> findAll(
            @PathVariable UUID workspaceId
    ) {
        return memoryService
                .findAll(workspaceId)
                .stream()
                .map(MemoryResponse::from)
                .toList();
    }

    @DeleteMapping("/{memoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID workspaceId,
            @PathVariable UUID memoryId
    ) {
        memoryService.delete(
                workspaceId,
                memoryId
        );
    }
}