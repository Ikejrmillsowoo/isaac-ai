package com.isaacai.ai.memory;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.model.MemoryCategory;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MemoryContextBuilderTest {
    @Test void selectsRelevantFactsAndPreferences() {
        Memory relevant = memory("Java project uses Spring Boot", MemoryCategory.PROJECT);
        Memory unrelated = memory("Garden irrigation", MemoryCategory.PROJECT);
        Memory preference = memory("I prefer concise answers", MemoryCategory.PREFERENCE);
        assertThat(new MemoryContextBuilder().build(List.of(unrelated, preference, relevant), "Java project"))
                .contains("Java project", "concise answers").doesNotContain("Garden irrigation");
    }
    @Test void boundsContext() {
        Memory large = memory("Java ".repeat(500), MemoryCategory.PROJECT);
        assertThat(new MemoryContextBuilder().build(List.of(large), "Java")).isEmpty();
    }
    @Test void retrievesSemanticMatchWithoutSharedWords() {
        Memory relevant = memory("Spring Boot application called Isaac AI", MemoryCategory.PROJECT);
        Memory unrelated = memory("Favorite fruit is mango", MemoryCategory.CONTEXT);
        var id = java.util.UUID.randomUUID();
        when(relevant.getId()).thenReturn(id);
        MemorySemanticIndex index = mock(MemorySemanticIndex.class);
        var memories = List.of(relevant, unrelated);
        when(index.similarities(memories, "software development project")).thenReturn(java.util.Map.of(id, .72));
        assertThat(new MemoryContextBuilder(index).build(memories, "software development project"))
                .contains("Isaac AI").doesNotContain("mango");
    }
    private Memory memory(String content, MemoryCategory category) {
        Memory m = mock(Memory.class);
        when(m.getContent()).thenReturn(content);
        when(m.getCategory()).thenReturn(category);
        return m;
    }
}
