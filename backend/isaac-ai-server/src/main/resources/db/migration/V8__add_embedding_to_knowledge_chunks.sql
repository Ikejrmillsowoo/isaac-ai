CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE knowledge_chunks
ADD COLUMN embedding vector(768);