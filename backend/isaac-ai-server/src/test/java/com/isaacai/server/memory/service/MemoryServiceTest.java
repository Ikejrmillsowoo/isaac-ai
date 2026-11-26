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



    @Test
    void shouldEditMemoryByIdWithinWorkspace() {
        UUID workspaceId = UUID.randomUUID();
        UUID memoryId = UUID.randomUUID();
        Memory memory = mock(Memory.class);
        when(memory.getId()).thenReturn(memoryId);
        when(memoryRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId))
                .thenReturn(List.of(memory));
        when(memoryRepository.save(memory)).thenReturn(memory);
        assertThat(memoryService.updateById(workspaceId, memoryId, "New content", MemoryCategory.GOAL))
                .isSameAs(memory);
        verify(memory).setContent("New content");
        verify(memory).setCategory(MemoryCategory.GOAL);
    }

    @Test
    void shouldRejectEditingMemoryOutsideWorkspace() {
        UUID workspaceId = UUID.randomUUID();
        when(memoryRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId)).thenReturn(List.of());
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                memoryService.updateById(workspaceId, UUID.randomUUID(), "New content", MemoryCategory.GOAL))
                .isInstanceOf(IllegalArgumentException.class);
        verify(memoryRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateContentWhenEditingById() {
        UUID workspaceId = UUID.randomUUID();
        UUID memoryId = UUID.randomUUID();
        Memory target = mock(Memory.class);
        Memory other = mock(Memory.class);
        when(target.getId()).thenReturn(memoryId);
        when(other.getId()).thenReturn(UUID.randomUUID());
        when(other.getContent()).thenReturn("I prefer Java examples.");
        when(memoryRepository.findAllByWorkspaceIdOrderByCreatedAtDesc(workspaceId))
                .thenReturn(List.of(target, other));
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                memoryService.updateById(workspaceId, memoryId, "I prefer Java examples", MemoryCategory.PREFERENCE))
                .isInstanceOf(IllegalArgumentException.class);
        verify(memoryRepository, never()).save(any());
    }
}
