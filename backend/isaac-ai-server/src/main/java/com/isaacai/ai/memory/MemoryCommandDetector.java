package com.isaacai.ai.memory;

import org.springframework.stereotype.Component;

@Component
public class MemoryCommandDetector {

    public boolean isRememberCommand(String message) {

        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized =
                message.trim().toLowerCase();

        return normalized.startsWith("remember that ")
                || normalized.startsWith("remember ")
                || normalized.startsWith("please remember that ")
                || normalized.startsWith("please remember ");
    }

    public String extractRememberContent(
            String message
    ) {

        if (!isRememberCommand(message)) {
            return "";
        }

        String trimmed = message.trim();

        String[] prefixes = {
                "please remember that ",
                "please remember ",
                "remember that ",
                "remember "
        };

        String lower = trimmed.toLowerCase();

        for (String prefix : prefixes) {

            if (lower.startsWith(prefix)) {

                return trimmed
                        .substring(prefix.length())
                        .trim();
            }
        }

        return "";
    }
}