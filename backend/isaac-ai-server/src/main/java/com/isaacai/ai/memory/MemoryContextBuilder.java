package com.isaacai.ai.memory;

import com.isaacai.server.memory.model.Memory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemoryContextBuilder {

    public String build(
            List<Memory> memories
    ) {
        if (memories == null || memories.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();

        context.append("""
                Relevant memories about the user:

                """);

        for (Memory memory : memories) {
            context.append("[")
                    .append(memory.getCategory())
                    .append("] ")
                    .append(memory.getContent())
                    .append("\n");
        }

        return context.toString().trim();
    }
}