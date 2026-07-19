CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_conversations_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_conversations_workspace_id
    ON conversations (workspace_id);

CREATE INDEX idx_conversations_workspace_archived
    ON conversations (workspace_id, archived);

CREATE INDEX idx_conversations_workspace_pinned
    ON conversations (workspace_id, pinned);