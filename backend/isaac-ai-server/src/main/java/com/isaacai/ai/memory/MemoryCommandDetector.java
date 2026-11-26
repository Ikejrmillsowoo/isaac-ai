package com.isaacai.ai.memory;

import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public boolean isForgetCommand(String message) {

    if (message == null || message.isBlank()) {
        return false;
    }

    String normalized =
            message.trim().toLowerCase();

    return normalized.startsWith("forget that ")
            || normalized.startsWith("forget ")
            || normalized.startsWith("please forget that ")
            || normalized.startsWith("please forget ");
}

public String extractForgetContent(
        String message
) {

    if (!isForgetCommand(message)) {
        return "";
    }

    String trimmed = message.trim();

    String[] prefixes = {
            "please forget that ",
            "please forget ",
            "forget that ",
            "forget "
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

private static final Pattern UPDATE_PATTERN =
        Pattern.compile(
                "^\\s*(?:please\\s+)?update\\s+my\\s+memory\\s+from\\s+\"([^\"]+)\"\\s+to\\s+\"([^\"]+)\"\\s*[.!?]?\\s*$",
                Pattern.CASE_INSENSITIVE
        );

public boolean isUpdateCommand(String message) {
    return extractUpdateCommand(message).isPresent();
}

public Optional<UpdateMemoryCommand> extractUpdateCommand(
        String message
) {
    if (message == null || message.isBlank()) {
        return Optional.empty();
    }

    Matcher matcher = UPDATE_PATTERN.matcher(message);

    if (!matcher.matches()) {
        return Optional.empty();
    }

    String originalContent = matcher.group(1).trim();
    String newContent = matcher.group(2).trim();

    if (originalContent.isBlank() || newContent.isBlank()) {
        return Optional.empty();
    }

    return Optional.of(
            new UpdateMemoryCommand(
                    originalContent,
                    newContent
            )
    );
}

}