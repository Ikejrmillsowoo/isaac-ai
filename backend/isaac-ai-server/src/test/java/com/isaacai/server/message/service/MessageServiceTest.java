package com.isaacai.server.message.service;

import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.message.exception.MessageNotFoundException;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import com.isaacai.server.message.repository.MessageRepository;
import com.isaacai.server.workspace.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.isaacai.support.TestDataFactory.assistantMessage;
import static com.isaacai.support.TestDataFactory.conversation;
import static com.isaacai.support.TestDataFactory.userMessage;
import static com.isaacai.support.TestDataFactory.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationService conversationService;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(
                messageRepository,
                conversationService
        );
    }

    @Test
    void shouldCreateUserMessage() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result = messageService.createUserMessage(
                workspace.getId(),
                conversation.getId(),
                "Explain dependency injection."
        );

        assertThat(result.getId()).isNotNull();
        assertThat(result.getConversation()).isSameAs(conversation);
        assertThat(result.getRole()).isEqualTo(MessageRole.USER);
        assertThat(result.getContent())
                .isEqualTo("Explain dependency injection.");

        ArgumentCaptor<Message> messageCaptor =
                ArgumentCaptor.forClass(Message.class);

        verify(messageRepository).save(messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();

        assertThat(savedMessage.getConversation())
                .isSameAs(conversation);
        assertThat(savedMessage.getRole())
                .isEqualTo(MessageRole.USER);
        assertThat(savedMessage.getContent())
                .isEqualTo("Explain dependency injection.");
    }

    @Test
    void shouldCreateAssistantMessageUsingInternalCreateMethod() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message result = messageService.create(
                workspace.getId(),
                conversation.getId(),
                MessageRole.ASSISTANT,
                "Dependency injection supplies required dependencies."
        );

        assertThat(result.getConversation()).isSameAs(conversation);
        assertThat(result.getRole())
                .isEqualTo(MessageRole.ASSISTANT);
        assertThat(result.getContent())
                .isEqualTo(
                        "Dependency injection supplies required dependencies."
                );

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void shouldVerifyConversationBeforeCreatingMessage() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        messageService.createUserMessage(
                workspace.getId(),
                conversation.getId(),
                "Hello"
        );

        verify(conversationService).findById(
                workspace.getId(),
                conversation.getId()
        );
    }

    @Test
    void shouldNotSaveMessageWhenConversationValidationFails() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        RuntimeException exception =
                new RuntimeException("Conversation validation failed.");

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenThrow(exception);

        assertThatThrownBy(() ->
                messageService.createUserMessage(
                        workspaceId,
                        conversationId,
                        "Hello"
                )
        ).isSameAs(exception);

        verify(messageRepository, never())
                .save(any(Message.class));
    }

    @Test
    void shouldFindMessageById() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);
        Message message = userMessage(conversation);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.findByIdAndConversationId(
                message.getId(),
                conversation.getId()
        )).thenReturn(Optional.of(message));

        Message result = messageService.findById(
                workspace.getId(),
                conversation.getId(),
                message.getId()
        );

        assertThat(result).isSameAs(message);

        verify(conversationService).findById(
                workspace.getId(),
                conversation.getId()
        );

        verify(messageRepository).findByIdAndConversationId(
                message.getId(),
                conversation.getId()
        );
    }

    @Test
    void shouldThrowMessageNotFoundExceptionWhenMessageDoesNotExist() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);
        UUID messageId = UUID.randomUUID();

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.findByIdAndConversationId(
                messageId,
                conversation.getId()
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                messageService.findById(
                        workspace.getId(),
                        conversation.getId(),
                        messageId
                )
        )
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessage(
                        "Message with ID '" + messageId
                                + "' was not found."
                );
    }

    @Test
    void shouldNotSearchForMessageWhenConversationValidationFails() {
        UUID workspaceId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        RuntimeException exception =
                new RuntimeException("Conversation validation failed.");

        when(conversationService.findById(
                workspaceId,
                conversationId
        )).thenThrow(exception);

        assertThatThrownBy(() ->
                messageService.findById(
                        workspaceId,
                        conversationId,
                        messageId
                )
        ).isSameAs(exception);

        verifyNoInteractions(messageRepository);
    }

    @Test
    void shouldReturnConversationMessagesInRepositoryOrder() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        Message userMessage = new Message(
                conversation,
                MessageRole.USER,
                "What is dependency injection?"
        );

        Message assistantMessage = new Message(
                conversation,
                MessageRole.ASSISTANT,
                "It is a way to supply object dependencies."
        );

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository
                .findAllByConversationIdOrderByCreatedAtAscIdAsc(
                        conversation.getId()
                ))
                .thenReturn(
                        List.of(userMessage, assistantMessage)
                );

        List<Message> results =
                messageService.findConversationMessages(
                        workspace.getId(),
                        conversation.getId()
                );

        assertThat(results)
                .containsExactly(userMessage, assistantMessage);

        verify(messageRepository)
                .findAllByConversationIdOrderByCreatedAtAscIdAsc(
                        conversation.getId()
                );
    }

    @Test
    void shouldReturnEmptyListWhenConversationHasNoMessages() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository
                .findAllByConversationIdOrderByCreatedAtAscIdAsc(
                        conversation.getId()
                ))
                .thenReturn(List.of());

        List<Message> results =
                messageService.findConversationMessages(
                        workspace.getId(),
                        conversation.getId()
                );

        assertThat(results).isEmpty();
    }

    @Test
    void shouldUpdateMessageContent() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);
        Message message = userMessage(conversation);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.findByIdAndConversationId(
                message.getId(),
                conversation.getId()
        )).thenReturn(Optional.of(message));

        Message result = messageService.update(
                workspace.getId(),
                conversation.getId(),
                message.getId(),
                "  Updated message content.  "
        );

        assertThat(result).isSameAs(message);
        assertThat(result.getContent())
                .isEqualTo("Updated message content.");

        verify(messageRepository, never())
                .save(any(Message.class));
    }

    @Test
    void shouldLeaveMessageUnchangedWhenUpdateContentIsNull() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);
        Message message = userMessage(conversation);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.findByIdAndConversationId(
                message.getId(),
                conversation.getId()
        )).thenReturn(Optional.of(message));

        Message result = messageService.update(
                workspace.getId(),
                conversation.getId(),
                message.getId(),
                null
        );

        assertThat(result).isSameAs(message);
        assertThat(result.getContent()).isEqualTo("Original message");

        verify(messageRepository, never())
                .save(any(Message.class));
    }

    @Test
    void shouldRejectBlankMessageContentDuringUpdate() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);
        Message message = userMessage(conversation);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.findByIdAndConversationId(
                message.getId(),
                conversation.getId()
        )).thenReturn(Optional.of(message));

        assertThatThrownBy(() ->
                messageService.update(
                        workspace.getId(),
                        conversation.getId(),
                        message.getId(),
                        "   "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message content is required.");

        assertThat(message.getContent())
                .isEqualTo("Original message");
    }

    @Test
    void shouldDeleteMessage() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);
        Message message = userMessage(conversation);

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.findByIdAndConversationId(
                message.getId(),
                conversation.getId()
        )).thenReturn(Optional.of(message));

        messageService.delete(
                workspace.getId(),
                conversation.getId(),
                message.getId()
        );

        verify(messageRepository).delete(message);
    }

    @Test
    void shouldNotDeleteMessageWhenMessageDoesNotExist() {
        Workspace workspace = workspace();
        Conversation conversation = conversation(workspace);
        UUID messageId = UUID.randomUUID();

        when(conversationService.findById(
                workspace.getId(),
                conversation.getId()
        )).thenReturn(conversation);

        when(messageRepository.findByIdAndConversationId(
                messageId,
                conversation.getId()
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                messageService.delete(
                        workspace.getId(),
                        conversation.getId(),
                        messageId
                )
        )
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessage(
                        "Message with ID '" + messageId
                                + "' was not found."
                );

        verify(messageRepository, never())
                .delete(any(Message.class));
    }
  
}