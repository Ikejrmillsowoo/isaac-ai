package com.isaacai.server.memory.repository;

import com.isaacai.server.memory.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemoryRepository
        extends JpaRepository<Memory, UUID> {

    List<Memory> findAllByWorkspaceIdOrderByCreatedAtDesc(
            UUID workspaceId
    );

    List<Memory> findAllByWorkspaceIdAndCategoryOrderByCreatedAtDesc(
            UUID workspaceId,
            com.isaacai.server.memory.model.MemoryCategory category
    );
}