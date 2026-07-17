package com.isaacai.server.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

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