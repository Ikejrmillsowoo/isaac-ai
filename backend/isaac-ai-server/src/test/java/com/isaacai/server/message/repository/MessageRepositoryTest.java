package com.isaacai.server.message.repository;

import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.repository.ConversationRepository;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
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
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Test
    void shouldFindMessagesForConversationInChronologicalOrder() {
        Conversation conversation = saveConversation(
                "Spring development"
        );

        Message first = messageRepository.save(
                new Message(
                        conversation,
                        MessageRole.USER,
                        "What is dependency injection?"
                )
        );

        Message second = messageRepository.save(
                new Message(
                        conversation,
                        MessageRole.ASSISTANT,
                        "Dependency injection supplies required dependencies."
                )
        );

        List<Message> results =
                messageRepository
                        .findAllByConversationIdOrderByCreatedAtAsc(
                                conversation.getId()
                        );

        assertThat(results).containsExactly(first, second);
    }

    @Test
    void shouldOnlyReturnMessagesFromRequestedConversation() {
        Conversation firstConversation = saveConversation(
                "Spring development"
        );

        Conversation secondConversation = saveConversation(
                "React development"
        );

        Message firstMessage = messageRepository.save(
                new Message(
                        firstConversation,
                        MessageRole.USER,
                        "Explain Spring Boot."
                )
        );

        messageRepository.save(
                new Message(
                        secondConversation,
                        MessageRole.USER,
                        "Explain React hooks."
                )
        );

        List<Message> results =
                messageRepository
                        .findAllByConversationIdOrderByCreatedAtAsc(
                                firstConversation.getId()
                        );

        assertThat(results).containsExactly(firstMessage);
    }

    @Test
    void shouldFindMessageByIdAndConversationId() {
        Conversation conversation = saveConversation(
                "Spring development"
        );

        Message message = messageRepository.save(
                new Message(
                        conversation,
                        MessageRole.USER,
                        "Explain Spring Boot."
                )
        );

        Optional<Message> result =
                messageRepository.findByIdAndConversationId(
                        message.getId(),
                        conversation.getId()
                );

        assertThat(result).contains(message);
    }

    @Test
    void shouldNotFindMessageUnderDifferentConversation() {
        Conversation firstConversation = saveConversation(
                "Spring development"
        );

        Conversation secondConversation = saveConversation(
                "React development"
        );

        Message message = messageRepository.save(
                new Message(
                        firstConversation,
                        MessageRole.USER,
                        "Explain Spring Boot."
                )
        );

        Optional<Message> result =
                messageRepository.findByIdAndConversationId(
                        message.getId(),
                        secondConversation.getId()
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCountMessagesForConversation() {
        Conversation conversation = saveConversation(
                "Spring development"
        );

        messageRepository.saveAll(
                List.of(
                        new Message(
                                conversation,
                                MessageRole.USER,
                                "Question one"
                        ),
                        new Message(
                                conversation,
                                MessageRole.ASSISTANT,
                                "Answer one"
                        ),
                        new Message(
                                conversation,
                                MessageRole.USER,
                                "Question two"
                        )
                )
        );

        long count = messageRepository.countByConversationId(
                conversation.getId()
        );

        assertThat(count).isEqualTo(3);
    }

    private Conversation saveConversation(String title) {
        Workspace workspace = workspaceRepository.save(
                new Workspace(
                        title + " workspace",
                        null,
                        null,
                        null
                )
        );

        return conversationRepository.save(
                new Conversation(workspace, title)
        );
    }
}