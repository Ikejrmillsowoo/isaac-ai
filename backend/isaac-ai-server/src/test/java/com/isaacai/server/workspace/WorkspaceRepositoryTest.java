package com.isaacai.server.workspace;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkspaceRepositoryTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

     @BeforeEach
    void cleanDatabase() {
        workspaceRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveWorkspace() {
        Workspace workspace = new Workspace(
                "Software",
                "Software engineering projects",
                "Act as a senior engineer",
                "#2563EB"
        );

        Workspace savedWorkspace =
                workspaceRepository.save(workspace);

        assertThat(savedWorkspace.getId()).isNotNull();

        Workspace foundWorkspace = workspaceRepository
                .findById(savedWorkspace.getId())
                .orElseThrow();

        assertThat(foundWorkspace.getName())
                .isEqualTo("Software");
        assertThat(foundWorkspace.getDescription())
                .isEqualTo("Software engineering projects");
    }

    @Test
    void shouldFindWorkspaceIgnoringNameCase() {
        Workspace workspace = new Workspace(
                "Research",
                null,
                null,
                null
        );

        workspaceRepository.save(workspace);

        boolean exists =
                workspaceRepository.existsByNameIgnoreCase("research");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnOnlyActiveWorkspaces() {
        Workspace activeWorkspace = new Workspace(
                "Software",
                null,
                null,
                null
        );

        Workspace archivedWorkspace = new Workspace(
                "Old Research",
                null,
                null,
                null
        );

        archivedWorkspace.archive();

        workspaceRepository.save(activeWorkspace);
        workspaceRepository.save(archivedWorkspace);

        var activeWorkspaces =
                workspaceRepository
                        .findAllByArchivedFalseOrderByCreatedAtAsc();

        assertThat(activeWorkspaces)
                .extracting(Workspace::getName)
                .containsExactly("Software");
    }
}