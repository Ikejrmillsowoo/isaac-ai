package com.isaacai.server.memory.service;

import com.isaacai.server.memory.model.Memory;
import com.isaacai.server.memory.model.MemoryCategory;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.memory.repository.MemoryRepository;
import com.isaacai.server.workspace.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoryServiceTest {

    @Mock
    private MemoryRepository memoryRepository;

    @Mock
    private WorkspaceService workspaceService;

    private MemoryService memoryService;

    @BeforeEach
    void setUp() {
        memoryService = new MemoryService(
                memoryRepository,
                workspaceService
        );
    }

    @Test
    void shouldForgetMatchingMemory() {

        UUID workspaceId = UUID.randomUUID();

        Memory memory = mock(Memory.class);

        when(memory.getContent())
        .thenReturn(
                "The user prefers Java examples instead of Python examples."
        );



        when(memoryRepository
                .findAllByWorkspaceIdOrderByCreatedAtDesc(
                        workspaceId
                ))
                .thenReturn(List.of(memory));

               boolean result =
        memoryService.forget(
                workspaceId,
                "I prefer Java examples instead of Python examples."
        );

        assertThat(result).isTrue();

        verify(memoryRepository)
                .deleteAll(List.of(memory));
    }

    
@Test
void shouldNotCreateDuplicateMemory() {

    UUID workspaceId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();

    String existingContent =
            "The user prefers Java examples instead of Python examples.";

    String incomingContent =
            "I prefer Java examples instead of Python examples.";

    Workspace workspace = mock(Workspace.class);
    Memory existingMemory = mock(Memory.class);

    when(workspaceService.findById(workspaceId))
            .thenReturn(workspace);

    when(existingMemory.getContent())
            .thenReturn(existingContent);

    when(memoryRepository
            .findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId))
            .thenReturn(List.of(existingMemory));

    Memory result = memoryService.create(
            workspaceId,
            incomingContent,
            MemoryCategory.PREFERENCE,
            conversationId
    );

    assertThat(result).isSameAs(existingMemory);

    verify(memoryRepository, never())
            .save(any(Memory.class));
}


@Test
void shouldUpdateExistingMemory() {

    UUID workspaceId = UUID.randomUUID();
    UUID memoryId = UUID.randomUUID();

    Memory existingMemory = mock(Memory.class);

    when(existingMemory.getId())
            .thenReturn(memoryId);

    when(existingMemory.getContent())
            .thenReturn("I prefer Java examples.");

    when(workspaceService.findById(workspaceId))
            .thenReturn(mock(Workspace.class));

    when(memoryRepository
            .findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId))
            .thenReturn(List.of(existingMemory));

    when(memoryRepository.save(existingMemory))
            .thenReturn(existingMemory);

    Memory result = memoryService.update(
            workspaceId,
            "I prefer Java examples.",
            "I prefer Python examples."
    );

    assertThat(result).isSameAs(existingMemory);

    verify(existingMemory)
            .setContent("I prefer Python examples.");

    verify(memoryRepository)
            .save(existingMemory);
}


}