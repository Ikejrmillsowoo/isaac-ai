package com.isaacai.server.workspace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.isaacai.server.workspace.model.Workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository
        extends JpaRepository<Workspace, UUID> {

    boolean existsByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Optional<Workspace> findByNameIgnoreCase(String name);

    List<Workspace> findAllByArchivedFalseOrderByCreatedAtAsc();

    List<Workspace> findAllByArchivedTrueOrderByCreatedAtAsc();
}