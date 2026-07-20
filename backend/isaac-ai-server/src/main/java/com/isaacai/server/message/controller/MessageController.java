package com.isaacai.server.message.controller;

import com.isaacai.server.message.dto.CreateMessageRequest;
import com.isaacai.server.message.dto.MessageResponse;
import com.isaacai.server.message.dto.UpdateMessageRequest;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/workspaces/{workspaceId}/conversations/{conversationId}/messages"
)
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId,
            @Valid @RequestBody CreateMessageRequest request
    ) {
        Message message = messageService.createUserMessage(
                workspaceId,
                conversationId,
                request.content()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MessageResponse.from(message));
    }

    @GetMapping
    public List<MessageResponse> getConversationMessages(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId
    ) {
        return messageService
                .findConversationMessages(
                        workspaceId,
                        conversationId
                )
                .stream()
                .map(MessageResponse::from)
                .toList();
    }

    @GetMapping("/{messageId}")
    public MessageResponse getMessage(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId
    ) {
        Message message = messageService.findById(
                workspaceId,
                conversationId,
                messageId
        );

        return MessageResponse.from(message);
    }

    @PutMapping("/{messageId}")
    public MessageResponse updateMessage(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @Valid @RequestBody UpdateMessageRequest request
    ) {
        Message message = messageService.update(
                workspaceId,
                conversationId,
                messageId,
                request.content()
        );

        return MessageResponse.from(message);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId
    ) {
        messageService.delete(
                workspaceId,
                conversationId,
                messageId
        );

        return ResponseEntity.noContent().build();
    }
}