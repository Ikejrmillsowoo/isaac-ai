package com.isaacai.server.workspace;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceTest {

    @Test
    void shouldCreateWorkspaceWithNormalizedValues() {
        Workspace workspace = new Workspace(
                "  Software  ",
                "  Java and Spring projects  ",
                "  Act as a senior software engineer  ",
                "  #2563EB  "
        );

        assertThat(workspace.getId()).isNotNull();
        assertThat(workspace.getName()).isEqualTo("Software");
        assertThat(workspace.getDescription())
                .isEqualTo("Java and Spring projects");
        assertThat(workspace.getSystemPrompt())
                .isEqualTo("Act as a senior software engineer");
        assertThat(workspace.getColor()).isEqualTo("#2563EB");
        assertThat(workspace.isArchived()).isFalse();
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() ->
                new Workspace(null, null, null, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Workspace name must not be blank");
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() ->
                new Workspace("   ", null, null, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Workspace name must not be blank");
    }

    @Test
    void shouldRejectNameLongerThanOneHundredCharacters() {
        String longName = "a".repeat(101);

        assertThatThrownBy(() ->
                new Workspace(longName, null, null, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Workspace name must not exceed 100 characters");
    }

    @Test
    void shouldConvertBlankOptionalValuesToNull() {
        Workspace workspace = new Workspace(
                "Research",
                "   ",
                "",
                null
        );

        assertThat(workspace.getDescription()).isNull();
        assertThat(workspace.getSystemPrompt()).isNull();
        assertThat(workspace.getColor()).isNull();
    }

    @Test
    void shouldRenameWorkspace() {
        Workspace workspace = new Workspace(
                "Software",
                null,
                null,
                null
        );

        workspace.rename("  Healthcare  ");

        assertThat(workspace.getName()).isEqualTo("Healthcare");
    }

    @Test
    void shouldArchiveWorkspace() {
        Workspace workspace = new Workspace(
                "Church",
                null,
                null,
                null
        );

        workspace.archive();

        assertThat(workspace.isArchived()).isTrue();
    }

    @Test
    void shouldRestoreWorkspace() {
        Workspace workspace = new Workspace(
                "Church",
                null,
                null,
                null
        );

        workspace.archive();
        workspace.restore();

        assertThat(workspace.isArchived()).isFalse();
    }

    @Test
    void shouldUpdateWorkspaceDetails() {
        Workspace workspace = new Workspace(
                "Before I Do",
                null,
                null,
                null
        );

        workspace.updateDescription(
                "Relationship education and writing"
        );
        workspace.updateSystemPrompt(
                "Help create relationship education content"
        );
        workspace.updateColor("#7C3AED");

        assertThat(workspace.getDescription())
                .isEqualTo("Relationship education and writing");
        assertThat(workspace.getSystemPrompt())
                .isEqualTo("Help create relationship education content");
        assertThat(workspace.getColor()).isEqualTo("#7C3AED");
    }
}