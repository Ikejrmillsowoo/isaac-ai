package com.isaacai.server.conversation.repository;

import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.workspace.model.Workspace;
import com.isaacai.server.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Test
    void shouldFindActiveConversationsForWorkspace() {
        Workspace software = saveWorkspace("Software");
        Workspace writing = saveWorkspace("Writing");

        Conversation first = new Conversation(
                software,
                "Spring architecture"
        );

        Conversation second = new Conversation(
                software,
                "React workspace UI"
        );

        second.pin();

        Conversation archived = new Conversation(
                software,
                "Old research"
        );

        archived.archive();

        Conversation otherWorkspaceConversation = new Conversation(
                writing,
                "Book notes"
        );

        conversationRepository.saveAll(List.of(
                first,
                second,
                archived,
                otherWorkspaceConversation
        ));

        List<Conversation> results =
                conversationRepository
                        .findAllByWorkspaceIdAndArchivedFalseOrderByPinnedDescUpdatedAtDesc(
                                software.getId()
                        );

        assertThat(results).hasSize(2);
        assertThat(results.getFirst().getTitle())
                .isEqualTo("React workspace UI");
        assertThat(results)
                .extracting(Conversation::getTitle)
                .containsExactly(
                        "React workspace UI",
                        "Spring architecture"
                );
    }

    @Test
    void shouldFindArchivedConversationsForWorkspace() {
        Workspace software = saveWorkspace("Software");

        Conversation active = new Conversation(
                software,
                "Active conversation"
        );

        Conversation archived = new Conversation(
                software,
                "Archived conversation"
        );

        archived.archive();

        conversationRepository.saveAll(
                List.of(active, archived)
        );

        List<Conversation> results =
                conversationRepository
                        .findAllByWorkspaceIdAndArchivedTrueOrderByUpdatedAtDesc(
                                software.getId()
                        );

        assertThat(results)
                .extracting(Conversation::getTitle)
                .containsExactly("Archived conversation");
    }

    @Test
    void shouldFindConversationByIdAndWorkspaceId() {
        Workspace software = saveWorkspace("Software");

        Conversation conversation = conversationRepository.save(
                new Conversation(
                        software,
                        "Spring architecture"
                )
        );

        Optional<Conversation> result =
                conversationRepository.findByIdAndWorkspaceId(
                        conversation.getId(),
                        software.getId()
                );

        assertThat(result).contains(conversation);
    }

    @Test
    void shouldNotFindConversationUnderDifferentWorkspace() {
        Workspace software = saveWorkspace("Software");
        Workspace writing = saveWorkspace("Writing");

        Conversation conversation = conversationRepository.save(
                new Conversation(
                        software,
                        "Spring architecture"
                )
        );

        Optional<Conversation> result =
                conversationRepository.findByIdAndWorkspaceId(
                        conversation.getId(),
                        writing.getId()
                );

        assertThat(result).isEmpty();
    }

    private Workspace saveWorkspace(String name) {
        return workspaceRepository.save(
                new Workspace(
                        name,
                        null,
                        null,
                        null
                )
        );
    }
}