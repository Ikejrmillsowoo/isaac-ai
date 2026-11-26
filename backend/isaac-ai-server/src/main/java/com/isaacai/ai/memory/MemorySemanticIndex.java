package com.isaacai.ai.memory;

import com.isaacai.ai.embedding.EmbeddingClient;
import com.isaacai.server.memory.model.Memory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class MemorySemanticIndex {
    private final JdbcTemplate jdbc;
    private final EmbeddingClient embeddings;

    public MemorySemanticIndex(JdbcTemplate jdbc, EmbeddingClient embeddings) {
        this.jdbc = jdbc;
        this.embeddings = embeddings;
    }

    public Map<UUID, Double> similarities(List<Memory> memories, String query) {
        Map<UUID, Double> scores = new HashMap<>();
        if (query == null || query.isBlank()) return scores;
        String vector = vector(embeddings.embed(query));
        for (Memory memory : memories) {
            if (memory.getId() == null || memory.getContent() == null || memory.getContent().isBlank()) continue;
            UUID id = memory.getId();
            UUID workspace = memory.getWorkspace().getId();
            String content = memory.getContent();
            Boolean current = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM memories WHERE id=? AND workspace_id=? AND content=? AND embedded_content=content AND embedding IS NOT NULL)",
                Boolean.class, id, workspace, content);
            if (!Boolean.TRUE.equals(current)) {
                String value = vector(embeddings.embed(content));
                jdbc.update("UPDATE memories SET embedding=CAST(? AS vector), embedded_content=content WHERE id=? AND workspace_id=? AND content=?",
                    value, id, workspace, content);
            }
            jdbc.query("SELECT 1 - (embedding <=> CAST(? AS vector)) AS similarity FROM memories WHERE id=? AND workspace_id=? AND content=? AND embedded_content=content AND embedding IS NOT NULL",
                rs -> { double similarity = rs.getDouble("similarity"); if (!rs.wasNull() && Double.isFinite(similarity)) scores.put(id, similarity); },
                vector, id, workspace, content);
        }
        return scores;
    }

    private static String vector(float[] values) {
        if (values == null || values.length != 768) throw new IllegalArgumentException("Expected 768 embedding dimensions");
        StringJoiner result = new StringJoiner(",", "[", "]");
        for (float value : values) {
            if (!Float.isFinite(value)) throw new IllegalArgumentException("Invalid embedding");
            result.add(Float.toString(value));
        }
        return result.toString();
    }
}
