package com.isaacai.ai.memory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryCommandDetectorTest {

    private final MemoryCommandDetector detector =
            new MemoryCommandDetector();

    @Test
    void shouldDetectRememberCommand() {

        boolean result =
                detector.isRememberCommand(
                        "Remember that I prefer Java."
                );

        assertThat(result).isTrue();
    }

    @Test
    void shouldExtractRememberContent() {

        String result =
                detector.extractRememberContent(
                        "Remember that I prefer Java."
                );

        assertThat(result)
                .isEqualTo(
                        "I prefer Java."
                );
    }

    @Test
    void shouldNotDetectNormalMessage() {

        boolean result =
                detector.isRememberCommand(
                        "Explain Java interfaces."
                );

        assertThat(result).isFalse();
    }
}