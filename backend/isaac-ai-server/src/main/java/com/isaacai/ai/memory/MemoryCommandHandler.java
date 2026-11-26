
package com.isaacai.ai.memory;

import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.model.MemoryCategory;
import com.isaacai.server.memory.service.MemoryService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MemoryCommandHandler {

    private final MemoryCommandDetector memoryCommandDetector;
    private final MemoryService memoryService;

    public MemoryCommandHandler(
            MemoryCommandDetector memoryCommandDetector,
            MemoryService memoryService
    ) {
        this.memoryCommandDetector = memoryCommandDetector;
        this.memoryService = memoryService;
    }

    public Optional<String> handle(ChatRequest request) {

        String message = request.message();

        // 1. Detect Update command
        Optional<UpdateMemoryCommand> updateCommand =
                memoryCommandDetector.extractUpdateCommand(message);

        if (updateCommand.isPresent()) {
            return handleUpdate(request, updateCommand.get());
        }

        // 2. Detect Remember command
        if (memoryCommandDetector.isRememberCommand(message)) {
            return handleRemember(request);
        }

        // 3. Detect Forget command
        if (memoryCommandDetector.isForgetCommand(message)) {
            return handleForget(request);
        }

        // 4. Ordinary conversation
        return Optional.empty();
    }

    private Optional<String> handleRemember(ChatRequest request) {

        String memoryContent =
                memoryCommandDetector.extractRememberContent(
                        request.message()
                );

        Memory savedMemory =
                memoryService.create(
                        request.workspaceId(),
                        memoryContent,
                        MemoryCategory.CONTEXT,
                        request.conversationId()
                );

        return Optional.of(
                "I'll remember that: " + savedMemory.getContent()
        );
    }

    private Optional<String> handleForget(ChatRequest request) {

        String memoryContent =
                memoryCommandDetector.extractForgetContent(
                        request.message()
                );

        boolean forgotten =
                memoryService.forget(
                        request.workspaceId(),
                        memoryContent
                );

        if (forgotten) {
            return Optional.of(
                    "I've removed that memory."
            );
        }

        return Optional.of(
                "I couldn't find a matching memory to remove."
        );
    }

    private Optional<String> handleUpdate(
            ChatRequest request,
            UpdateMemoryCommand command
    ) {

        try {

            Memory updatedMemory =
                    memoryService.update(
                            request.workspaceId(),
                            command.originalContent(),
                            command.newContent()
                    );

            return Optional.of(
                    "Your memory has been updated: "
                            + updatedMemory.getContent()
            );

        } catch (IllegalArgumentException |
                 IllegalStateException exception) {

            return Optional.of(
                    "Unable to update your memory. "
                            + exception.getMessage()
            );
        }
    }
}
