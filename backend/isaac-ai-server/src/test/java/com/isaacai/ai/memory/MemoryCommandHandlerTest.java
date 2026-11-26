
package com.isaacai.ai.memory;

import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.model.MemoryCategory;
import com.isaacai.server.memory.service.MemoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoryCommandHandlerTest {

    @Mock
    private MemoryCommandDetector memoryCommandDetector;

    @Mock
    private MemoryService memoryService;

    @Mock
    private Memory memory;

    private MemoryCommandHandler memoryCommandHandler;

    private UUID workspaceId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {

        memoryCommandHandler = new MemoryCommandHandler(
                memoryCommandDetector,
                memoryService
        );

        workspaceId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
    }

    @Test
    void shouldReturnEmptyForOrdinaryMessage() {

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Show me a Java example."
        );

        Optional<String> response =
                memoryCommandHandler.handle(request);

        assertThat(response).isEmpty();

        verifyNoInteractions(memoryService);
    }

    @Test
    void shouldHandleRememberCommand() {

        String content = "I prefer Java.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Remember that " + content
        );

        when(memoryCommandDetector.isRememberCommand(
                request.message()
        )).thenReturn(true);

        when(memoryCommandDetector.extractRememberContent(
                request.message()
        )).thenReturn(content);

        when(memoryService.create(
                workspaceId,
                content,
                MemoryCategory.CONTEXT,
                conversationId
        )).thenReturn(memory);

        when(memory.getContent())
                .thenReturn(content);

        Optional<String> response =
                memoryCommandHandler.handle(request);

        assertThat(response)
                .contains("I'll remember that: " + content);

        verify(memoryService).create(
                workspaceId,
                content,
                MemoryCategory.CONTEXT,
                conversationId
        );

        verifyNoMoreInteractions(memoryService);
    }

    @Test
    void shouldHandleSuccessfulForgetCommand() {

        String content = "I prefer Java.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Forget that " + content
        );

        when(memoryCommandDetector.isForgetCommand(
                request.message()
        )).thenReturn(true);

        when(memoryCommandDetector.extractForgetContent(
                request.message()
        )).thenReturn(content);

        when(memoryService.forget(
                workspaceId,
                content
        )).thenReturn(true);

        Optional<String> response =
                memoryCommandHandler.handle(request);

        assertThat(response)
                .contains("I've removed that memory.");

        verify(memoryService).forget(
                workspaceId,
                content
        );

        verifyNoMoreInteractions(memoryService);
    }

    @Test
    void shouldHandleForgetWhenMemoryDoesNotExist() {

        String content = "I prefer Java.";

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Forget that " + content
        );

        when(memoryCommandDetector.isForgetCommand(
                request.message()
        )).thenReturn(true);

        when(memoryCommandDetector.extractForgetContent(
                request.message()
        )).thenReturn(content);

        when(memoryService.forget(
                workspaceId,
                content
        )).thenReturn(false);

        Optional<String> response =
                memoryCommandHandler.handle(request);

        assertThat(response).contains(
                "I couldn't find a matching memory to remove."
        );

        verify(memoryService).forget(
                workspaceId,
                content
        );

        verifyNoMoreInteractions(memoryService);
    }

    @Test
    void shouldHandleSuccessfulUpdateCommand() {

        String originalContent = "I prefer Java.";
        String newContent = "I prefer Python.";

        ChatRequest request = prepareUpdateRequest(
                originalContent,
                newContent
        );

        when(memoryService.update(
                workspaceId,
                originalContent,
                newContent
        )).thenReturn(memory);

        when(memory.getContent())
                .thenReturn(newContent);

        Optional<String> response =
                memoryCommandHandler.handle(request);

        assertThat(response).contains(
                "Your memory has been updated: " + newContent
        );

        verify(memoryService).update(
                workspaceId,
                originalContent,
                newContent
        );

        verifyNoMoreInteractions(memoryService);
    }

    @Test
    void shouldRejectUpdateWhenOriginalMemoryDoesNotExist() {

        verifyRejectedUpdate(
                new IllegalArgumentException(
                        "Original memory not found."
                )
        );
    }

    @Test
    void shouldRejectUpdateWhenReplacementAlreadyExists() {

        verifyRejectedUpdate(
                new IllegalArgumentException(
                        "Another memory already contains the new content."
                )
        );
    }

    @Test
    void shouldRejectUpdateWhenMultipleMemoriesMatch() {

        verifyRejectedUpdate(
                new IllegalStateException(
                        "Multiple memories match the original content."
                )
        );
    }

    // Helper method for preparing update requests
    private ChatRequest prepareUpdateRequest(
            String originalContent,
            String newContent
    ) {

        ChatRequest request = new ChatRequest(
                workspaceId,
                conversationId,
                "Update my memory from \"" + originalContent
                        + "\" to \"" + newContent + "\"."
        );

        UpdateMemoryCommand command =
                new UpdateMemoryCommand(
                        originalContent,
                        newContent
                );

        when(memoryCommandDetector.extractUpdateCommand(
                request.message()
        )).thenReturn(Optional.of(command));

        return request;
    }

    // Reusable helper for unsuccessful updates
    private void verifyRejectedUpdate(
            RuntimeException exception
    ) {

        String originalContent = "I prefer Java.";
        String newContent = "I prefer Python.";

        ChatRequest request = prepareUpdateRequest(
                originalContent,
                newContent
        );

        when(memoryService.update(
                workspaceId,
                originalContent,
                newContent
        )).thenThrow(exception);

        Optional<String> response =
                memoryCommandHandler.handle(request);

        assertThat(response).contains(
                "Unable to update your memory. "
                        + exception.getMessage()
        );

        verify(memoryService).update(
                workspaceId,
                originalContent,
                newContent
        );

        verifyNoMoreInteractions(memoryService);
    }
}
