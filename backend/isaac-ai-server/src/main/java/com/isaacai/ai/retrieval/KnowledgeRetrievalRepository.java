package com.isaacai.ai.retrieval;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class KnowledgeRetrievalRepository {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeRetrievalRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RetrievedKnowledge> findSimilar(
            UUID workspaceId,
            String embedding,
            int limit
    ) {

        String sql = """
                SELECT
                    kc.id AS chunk_id,
                    kd.id AS document_id,
                    kd.name AS document_name,
                    kc.chunk_index,
                    kc.content,
                    kc.embedding <=> CAST(? AS vector) AS distance
                FROM knowledge_chunks kc
                JOIN knowledge_documents kd
                    ON kd.id = kc.document_id
                WHERE kd.workspace_id = ?
                  AND kc.embedding IS NOT NULL
                ORDER BY kc.embedding <=> CAST(? AS vector)
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new RetrievedKnowledge(
                                rs.getObject(
                                        "chunk_id",
                                        UUID.class
                                ),
                                rs.getObject(
                                        "document_id",
                                        UUID.class
                                ),
                                rs.getString(
                                        "document_name"
                                ),
                                rs.getInt(
                                        "chunk_index"
                                ),
                                rs.getString(
                                        "content"
                                ),
                                rs.getDouble(
                                        "distance"
                                )
                        ),
                embedding,
                workspaceId,
                embedding,
                limit
        );
    }
}