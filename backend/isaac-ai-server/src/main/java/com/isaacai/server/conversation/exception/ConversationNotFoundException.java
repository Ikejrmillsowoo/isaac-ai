package com.isaacai.server.conversation.exception;

import java.util.UUID;

public class ConversationNotFoundException
        extends RuntimeException {

    public ConversationNotFoundException(UUID id) {
        super(
                "Conversation with ID '"
                        + id
                        + "' was not found."
        );
    }
}