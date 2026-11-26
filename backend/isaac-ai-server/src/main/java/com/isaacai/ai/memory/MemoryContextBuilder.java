package com.isaacai.ai.memory;

import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.model.MemoryCategory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class MemoryContextBuilder {
    private final MemorySemanticIndex semanticIndex;
    public MemoryContextBuilder() { this.semanticIndex = null; }
    @Autowired
    public MemoryContextBuilder(MemorySemanticIndex semanticIndex) { this.semanticIndex = semanticIndex; }

    private static final int MAX_CHARS = 1200;
    private static final int MAX_MEMORIES = 8;
    private static final Set<String> STOP = Set.of("the", "and", "for", "with", "this", "that", "what", "how", "you", "your", "are", "can", "please", "about", "user", "prefer", "prefers");

    public String build(List<Memory> memories) {
        return build(memories, "");
    }

    public String build(List<Memory> memories, String query) {
        if (memories == null || memories.isEmpty()) return "";
        Set<String> terms = tokens(query);
        Map<UUID, Double> semantic = Map.of();
        if (semanticIndex != null) {
            try { semantic = semanticIndex.similarities(memories, query); }
            catch (RuntimeException failure) {
                org.slf4j.LoggerFactory.getLogger(MemoryContextBuilder.class).warn("Semantic memory retrieval unavailable; using lexical ranking: {}", failure.getClass().getSimpleName());
            }
        }
        final Map<UUID, Double> similarities = semantic;
        var ranked = memories.stream()
                .filter(m -> m.getContent() != null && !m.getContent().isBlank())
                .map(m -> new Ranked(m, score(m, terms) + semanticScore(m, similarities)))
                .filter(r -> r.score() > 0)
                .sorted(Comparator.comparingDouble(Ranked::score).reversed())
                .limit(MAX_MEMORIES).toList();
        StringBuilder context = new StringBuilder();
        for (Ranked rankedMemory : ranked) {
            Memory memory = rankedMemory.memory();
            String line = "[" + memory.getCategory() + "] " + memory.getContent() + "\n";
            if (context.length() + line.length() <= MAX_CHARS) context.append(line);
        }
        return context.toString().trim();
    }

    private static int score(Memory memory, Set<String> terms) {
        Set<String> words = tokens(memory.getContent());
        int overlap = (int) terms.stream().filter(words::contains).count();
        return overlap * 3 + (memory.getCategory() == MemoryCategory.PREFERENCE ? 1 : 0);
    }

    private static Set<String> tokens(String text) {
        if (text == null) return Set.of();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(w -> w.length() >= 3 && !STOP.contains(w))
                .collect(Collectors.toSet());
    }

    private static double semanticScore(Memory memory, Map<UUID, Double> similarities) {
        if (memory.getId() == null) return 0;
        double similarity = similarities.getOrDefault(memory.getId(), 0.0);
        return similarity >= 0.45 ? similarity * 5 : 0;
    }

    private record Ranked(Memory memory, double score) {}
}
