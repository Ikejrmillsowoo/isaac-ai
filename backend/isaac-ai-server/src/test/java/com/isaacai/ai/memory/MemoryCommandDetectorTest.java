package com.isaacai.ai.memory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
void shouldDetectForgetCommand() {

    boolean result =
            detector.isForgetCommand(
                    "Forget that I prefer Java."
            );

    assertThat(result).isTrue();
}

@Test
void shouldExtractForgetContent() {

    String result =
            detector.extractForgetContent(
                    "Forget that I prefer Java."
            );

    assertThat(result)
            .isEqualTo("I prefer Java.");
}

@Test
void shouldNotDetectNormalMessageAsForgetCommand() {

    boolean result =
            detector.isForgetCommand(
                    "I forgot how Java interfaces work."
            );

    assertThat(result).isFalse();
}


@Test
void shouldDetectAndExtractUpdateCommand() {

    String message =
            "Update my memory from \"I prefer Java examples\" "
            + "to \"I prefer Python examples\".";

    assertTrue(detector.isUpdateCommand(message));

    UpdateMemoryCommand command =
            detector.extractUpdateCommand(message)
                    .orElseThrow();

    assertEquals(
            "I prefer Java examples",
            command.originalContent()
    );

    assertEquals(
            "I prefer Python examples",
            command.newContent()
    );
}

@Test
void shouldDetectPoliteUpdateCommand() {

    String message =
            "Please update my memory from \"I prefer Java\" "
            + "to \"I prefer Python\".";

    assertTrue(detector.isUpdateCommand(message));
}

@Test
void shouldRejectIncompleteUpdateCommand() {

    String message =
            "Update my memory from Java to Python";

    assertFalse(detector.isUpdateCommand(message));

    assertTrue(
            detector.extractUpdateCommand(message).isEmpty()
    );
}

@Test
void shouldNotConfuseUpdateWithRememberOrForget() {

    String message =
            "Update my memory from \"Java\" to \"Python\".";

    assertFalse(detector.isRememberCommand(message));
    assertFalse(detector.isForgetCommand(message));
    assertTrue(detector.isUpdateCommand(message));
}

}