package com.isaacai.server.message.model;

import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.workspace.model.Workspace;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageTest {

    @Test
    void shouldCreateUserMessage() {
        Conversation conversation = createConversation();

        Message message = new Message(
                conversation,
                MessageRole.USER,
                "How do I deploy this application?"
        );

        assertThat(message.getId()).isNotNull();
        assertThat(message.getConversation())
                .isSameAs(conversation);
        assertThat(message.getRole())
                .isEqualTo(MessageRole.USER);
        assertThat(message.getContent())
                .isEqualTo(
                        "How do I deploy this application?"
                );
    }

    @Test
    void shouldCreateAssistantMessage() {
        Message message = new Message(
                createConversation(),
                MessageRole.ASSISTANT,
                "You can deploy it using Docker."
        );

        assertThat(message.getRole())
                .isEqualTo(MessageRole.ASSISTANT);
    }

    @Test
    void shouldNormalizeContent() {
        Message message = new Message(
                createConversation(),
                MessageRole.USER,
                "  Explain Spring Boot  "
        );

        assertThat(message.getContent())
                .isEqualTo("Explain Spring Boot");
    }

    @Test
    void shouldRejectMissingConversation() {
        assertThatThrownBy(() ->
                new Message(
                        null,
                        MessageRole.USER,
                        "Hello"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Message conversation is required."
                );
    }

    @Test
    void shouldRejectMissingRole() {
        assertThatThrownBy(() ->
                new Message(
                        createConversation(),
                        null,
                        "Hello"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message role is required.");
    }

    @Test
    void shouldRejectNullContent() {
        assertThatThrownBy(() ->
                new Message(
                        createConversation(),
                        MessageRole.USER,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message content is required.");
    }

    @Test
    void shouldRejectBlankContent() {
        assertThatThrownBy(() ->
                new Message(
                        createConversation(),
                        MessageRole.USER,
                        "   "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message content is required.");
    }

    @Test
    void shouldUpdateContent() {
        Message message = new Message(
                createConversation(),
                MessageRole.USER,
                "Old content"
        );

        message.updateContent("  Updated content  ");

        assertThat(message.getContent())
                .isEqualTo("Updated content");
    }

    private Conversation createConversation() {
        Workspace workspace = new Workspace(
                "Software",
                null,
                null,
                null
        );

        return new Conversation(
                workspace,
                "Spring development"
        );
    }
}