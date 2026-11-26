package com.isaacai.ai.memory;

import java.util.Optional;
import java.util.regex.Pattern;

public final class AutomaticMemoryExtractor {
    private AutomaticMemoryExtractor() {}

    // Deliberately limited to explicit, durable technical response preferences.
    private static final Pattern PREFERENCE = Pattern.compile(
            "^I (?:always )?prefer (?:Java|Python|JavaScript|TypeScript|SQL|Kotlin|Go|Rust|C\\+\\+) (?:code )?examples[.!]?$"
            + "|^I (?:always )?prefer (?:concise|detailed|step-by-step) (?:answers|explanations|responses)[.!]?$",
            Pattern.CASE_INSENSITIVE);

    public static Optional<String> extract(String message) {
        if (message == null) return Optional.empty();
        String content = message.trim();
        return PREFERENCE.matcher(content).matches() ? Optional.of(content) : Optional.empty();
    }
}
