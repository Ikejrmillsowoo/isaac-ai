package com.isaacai.support;

import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import com.isaacai.server.workspace.model.Workspace;

public final class TestDataFactory {

    private TestDataFactory() {
        // Utility class.
    }

    public static Workspace workspace() {
        return workspace("Software");
    }

    public static Workspace workspace(String name) {
        return new Workspace(
                name,
                null,
                null,
                null
        );
    }

    public static Conversation conversation() {
        return conversation(
                workspace(),
                "Spring Boot"
        );
    }

    public static Conversation conversation(
            Workspace workspace
    ) {
        return conversation(
                workspace,
                "Spring Boot"
        );
    }

    public static Conversation conversation(
            Workspace workspace,
            String title
    ) {
        return new Conversation(
                workspace,
                title
        );
    }

    public static Message userMessage() {
        return userMessage(conversation());
    }

    public static Message userMessage(
            Conversation conversation
    ) {
        return userMessage(
                conversation,
                "Original message"
        );
    }

    public static Message userMessage(
            Conversation conversation,
            String content
    ) {
        return new Message(
                conversation,
                MessageRole.USER,
                content
        );
    }

    public static Message assistantMessage() {
        return assistantMessage(conversation());
    }

    public static Message assistantMessage(
            Conversation conversation
    ) {
        return assistantMessage(
                conversation,
                "Assistant response"
        );
    }

    public static Message assistantMessage(
            Conversation conversation,
            String content
    ) {
        return new Message(
                conversation,
                MessageRole.ASSISTANT,
                content
        );
    }
}