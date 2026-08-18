CREATE TABLE knowledge_documents (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255),
    content_type VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_knowledge_documents_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
);