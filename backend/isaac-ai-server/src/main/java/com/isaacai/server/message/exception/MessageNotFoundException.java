package com.isaacai.server.message.exception;

import java.util.UUID;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(UUID id) {
        super("Message with ID '" + id + "' was not found.");
    }
}