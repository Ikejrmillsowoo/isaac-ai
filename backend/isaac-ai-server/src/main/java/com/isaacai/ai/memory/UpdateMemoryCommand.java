
package com.isaacai.ai.memory;

public record UpdateMemoryCommand(
        String originalContent,
        String newContent
) {
}
