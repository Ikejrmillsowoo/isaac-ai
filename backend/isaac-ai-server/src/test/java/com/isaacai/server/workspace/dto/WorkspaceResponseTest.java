package com.isaacai.server.workspace.dto;

import org.junit.jupiter.api.Test;

import com.isaacai.server.workspace.model.Workspace;

import static org.assertj.core.api.Assertions.assertThat;

class WorkspaceResponseTest {

    @Test
    void shouldMapWorkspaceEntityToResponse() {
        Workspace workspace = new Workspace(
                "Software",
                "Software engineering projects",
                "Act as a senior software engineer",
                "#2563EB"
        );

        WorkspaceResponse response = WorkspaceResponse.from(workspace);

        assertThat(response.id()).isEqualTo(workspace.getId());
        assertThat(response.name()).isEqualTo("Software");
        assertThat(response.description())
                .isEqualTo("Software engineering projects");
        assertThat(response.systemPrompt())
                .isEqualTo("Act as a senior software engineer");
        assertThat(response.color()).isEqualTo("#2563EB");
        assertThat(response.archived()).isFalse();
    }
}