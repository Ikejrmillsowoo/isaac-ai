CREATE TABLE memories (
    id UUID PRIMARY KEY,

    workspace_id UUID NOT NULL,

    content TEXT NOT NULL,

    category VARCHAR(50) NOT NULL,

    source_conversation_id UUID,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_memories_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_memories_workspace_id
    ON memories(workspace_id);

CREATE INDEX idx_memories_category
    ON memories(category);