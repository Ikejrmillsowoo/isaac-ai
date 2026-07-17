package com.isaacai.server.workspace;

import java.util.UUID;

public class WorkspaceNotFoundException extends RuntimeException {

    public WorkspaceNotFoundException(UUID id) {
        super("Workspace with ID '" + id + "' was not found");
    }
}