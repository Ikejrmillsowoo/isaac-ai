package com.isaacai.ai.retrieval;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
class KnowledgeContextBuilderTest {
    @Test void labelsEvidenceAndSkipsOversizedChunks() {
        var small = new RetrievedKnowledge(UUID.randomUUID(), UUID.randomUUID(), "guide.pdf", 1, "Useful evidence", .2);
        var large = new RetrievedKnowledge(UUID.randomUUID(), UUID.randomUUID(), "large.pdf", 2, "x".repeat(5000), .3);
        assertThat(new KnowledgeContextBuilder().build(List.of(small, large)))
                .contains("[K1]", "guide.pdf", "Useful evidence").doesNotContain("large.pdf");
    }
}
