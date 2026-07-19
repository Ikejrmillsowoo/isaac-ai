package com.isaacai.server.conversation.model;

import com.isaacai.server.workspace.model.Workspace;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationTest {

    @Test
    void shouldCreateConversation() {
        Workspace workspace = createWorkspace();

        Conversation conversation = new Conversation(
                workspace,
                "Spring architecture"
        );

        assertThat(conversation.getId()).isNotNull();
        assertThat(conversation.getWorkspace()).isSameAs(workspace);
        assertThat(conversation.getTitle())
                .isEqualTo("Spring architecture");
        assertThat(conversation.isPinned()).isFalse();
        assertThat(conversation.isArchived()).isFalse();
    }

    @Test
    void shouldNormalizeTitle() {
        Conversation conversation = new Conversation(
                createWorkspace(),
                "  Spring architecture  "
        );

        assertThat(conversation.getTitle())
                .isEqualTo("Spring architecture");
    }

    @Test
    void shouldRejectMissingWorkspace() {
        assertThatThrownBy(() ->
                new Conversation(null, "Research")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conversation workspace is required.");
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThatThrownBy(() ->
                new Conversation(createWorkspace(), "   ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conversation title is required.");
    }

    @Test
    void shouldRejectTitleLongerThanTwoHundredCharacters() {
        String title = "a".repeat(201);

        assertThatThrownBy(() ->
                new Conversation(createWorkspace(), title)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Conversation title must not exceed 200 characters."
                );
    }

    @Test
    void shouldRenameConversation() {
        Conversation conversation = new Conversation(
                createWorkspace(),
                "Old title"
        );

        conversation.rename("  New title  ");

        assertThat(conversation.getTitle()).isEqualTo("New title");
    }

    @Test
    void shouldPinAndUnpinConversation() {
        Conversation conversation = new Conversation(
                createWorkspace(),
                "Research"
        );

        conversation.pin();

        assertThat(conversation.isPinned()).isTrue();

        conversation.unpin();

        assertThat(conversation.isPinned()).isFalse();
    }

    @Test
    void shouldArchiveAndRestoreConversation() {
        Conversation conversation = new Conversation(
                createWorkspace(),
                "Research"
        );

        conversation.archive();

        assertThat(conversation.isArchived()).isTrue();

        conversation.restore();

        assertThat(conversation.isArchived()).isFalse();
    }

    private Workspace createWorkspace() {
        return new Workspace(
                "Software",
                null,
                null,
                null
        );
    }
}