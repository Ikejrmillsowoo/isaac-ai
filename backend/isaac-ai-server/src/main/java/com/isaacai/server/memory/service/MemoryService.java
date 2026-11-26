
package com.isaacai.server.memory.service;

import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.model.MemoryCategory;
import com.isaacai.server.memory.repository.MemoryRepository;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
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

    // =====================================================
    // CREATE MEMORY
    // =====================================================

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

        String normalizedContent = normalize(content);

        List<Memory> existingMemories =
                memoryRepository
                        .findAllByWorkspaceIdOrderByCreatedAtDesc(
                                workspaceId
                        );

        for (Memory existingMemory : existingMemories) {

            if (normalize(existingMemory.getContent())
                    .equals(normalizedContent)) {

                // Upgrade generic CONTEXT to a specific category
                if (existingMemory.getCategory() == MemoryCategory.CONTEXT
                        && category != MemoryCategory.CONTEXT) {

                    existingMemory.setCategory(category);

                    return memoryRepository.save(existingMemory);
                }

                // Duplicate found: return the existing memory
                return existingMemory;
            }
        }

        // No duplicate: create a new memory
        Memory memory = new Memory(
                workspace,
                content.trim(),
                category,
                sourceConversationId
        );

        return memoryRepository.save(memory);
    }

    // =====================================================
    // UPDATE MEMORY
    // =====================================================

    public Memory update(
            UUID workspaceId,
            String originalContent,
            String newContent
    ) {
        if (originalContent == null || originalContent.isBlank()) {
            throw new IllegalArgumentException(
                    "Original memory content is required."
            );
        }

        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException(
                    "New memory content is required."
            );
        }

        List<Memory> memories = findAll(workspaceId);

        String normalizedOriginal = normalize(originalContent);
        String normalizedNew = normalize(newContent);

        // Find memories matching the original content
        List<Memory> matches = memories.stream()
                .filter(memory ->
                        normalize(memory.getContent())
                                .equals(normalizedOriginal)
                )
                .toList();

        if (matches.isEmpty()) {
            throw new IllegalArgumentException(
                    "Original memory not found."
            );
        }

        // Do not arbitrarily update one of several duplicates
        if (matches.size() > 1) {
            throw new IllegalStateException(
                    "Multiple memories match the original content."
            );
        }

        Memory existingMemory = matches.get(0);

        // Prevent updating to content already stored elsewhere
        boolean duplicateExists = memories.stream()
                .filter(memory ->
                        !memory.getId().equals(existingMemory.getId())
                )
                .anyMatch(memory ->
                        normalize(memory.getContent())
                                .equals(normalizedNew)
                );

        if (duplicateExists) {
            throw new IllegalArgumentException(
                    "Another memory already contains the new content."
            );
        }

        // No change needed if the content is equivalent
        if (normalizedOriginal.equals(normalizedNew)) {
            return existingMemory;
        }

        // Update the existing memory
        existingMemory.setContent(newContent.trim());

        return memoryRepository.save(existingMemory);
    }

    public Memory updateById(UUID workspaceId, UUID memoryId, String content, MemoryCategory category) {
        if (content == null || content.isBlank() || category == null) {
            throw new IllegalArgumentException("Memory content and category are required.");
        }
        List<Memory> memories = findAll(workspaceId);
        Memory target = memories.stream().filter(m -> memoryId.equals(m.getId()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Memory not found in this workspace."));
        if (memories.stream().filter(m -> !memoryId.equals(m.getId()))
                .anyMatch(m -> normalize(m.getContent()).equals(normalize(content)))) {
            throw new IllegalArgumentException("Another memory already contains the new content.");
        }
        target.setContent(content.trim());
        target.setCategory(category);
        return memoryRepository.save(target);
    }

    // =====================================================
    // GET ALL MEMORIES FOR A WORKSPACE
    // =====================================================

    @Transactional(readOnly = true)
    public List<Memory> findAll(UUID workspaceId) {

        workspaceService.findById(workspaceId);

        return memoryRepository
                .findAllByWorkspaceIdOrderByCreatedAtDesc(
                        workspaceId
                );
    }

    // =====================================================
    // DELETE MEMORY BY ID
    // =====================================================

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

        // Ensure the memory belongs to this workspace
        if (!memory.getWorkspace()
                .getId()
                .equals(workspaceId)) {

            throw new IllegalArgumentException(
                    "Memory does not belong to this workspace."
            );
        }

        memoryRepository.delete(memory);
    }

    // =====================================================
    // FORGET MEMORY BY CONTENT
    // =====================================================

    public boolean forget(
            UUID workspaceId,
            String content
    ) {
        if (content == null || content.isBlank()) {
            return false;
        }

        List<Memory> memories = findAll(workspaceId);

        String normalizedTarget = normalize(content);

        List<Memory> matchingMemories = memories.stream()
                .filter(memory ->
                        normalize(memory.getContent())
                                .equals(normalizedTarget)
                )
                .toList();

        if (matchingMemories.isEmpty()) {
            return false;
        }

        // Remove all matching duplicates
        memoryRepository.deleteAll(matchingMemories);

        return true;
    }

    // =====================================================
    // NORMALIZE MEMORY CONTENT
    // =====================================================

    private String normalize(String value) {

        String normalized = value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[.!?]+$", "")
                .trim();

        normalized = normalized
                .replaceFirst("^the user prefers ", "prefer ")
                .replaceFirst("^user prefers ", "prefer ")
                .replaceFirst("^i prefer ", "prefer ");

        return normalized.trim();
    }
}
