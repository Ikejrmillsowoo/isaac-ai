package com.isaacai.server.workspace.exception;

public class WorkspaceAlreadyExistsException extends RuntimeException {
    
    public WorkspaceAlreadyExistsException(String name) {
        super("Workspace '" + name + "' already exists");
    }
}
