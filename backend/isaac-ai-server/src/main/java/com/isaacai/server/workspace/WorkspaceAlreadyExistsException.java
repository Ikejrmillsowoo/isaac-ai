package com.isaacai.server.workspace;

public class WorkspaceAlreadyExistsException extends RuntimeException {
    
    public WorkspaceAlreadyExistsException(String name) {
        super("Workspace '" + name + "' already exists");
    }
}
