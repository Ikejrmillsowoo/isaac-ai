CREATE TABLE knowledge_chunks (
    id UUID PRIMARY KEY,

    document_id UUID NOT NULL,

    chunk_index INTEGER NOT NULL,

    content TEXT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_knowledge_chunks_document
        FOREIGN KEY (document_id)
        REFERENCES knowledge_documents(id),

    CONSTRAINT uq_knowledge_chunks_document_index
        UNIQUE (document_id, chunk_index)
);

CREATE INDEX idx_knowledge_chunks_document_id
    ON knowledge_chunks(document_id);