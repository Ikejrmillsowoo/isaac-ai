package com.isaacai.server.conversation.controller;

import com.isaacai.server.conversation.dto.CreateConversationRequest;
import com.isaacai.server.conversation.dto.ConversationResponse;
import com.isaacai.server.conversation.dto.UpdateConversationRequest;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService
    ) {
        this.conversationService = conversationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateConversationRequest request
    ) {
        Conversation conversation =
                conversationService.create(
                        workspaceId,
                        request.title()
                );

        return ConversationResponse.from(conversation);
    }

    @GetMapping
    public List<ConversationResponse> findActive(
            @PathVariable UUID workspaceId
    ) {
        return conversationService.findActive(workspaceId)
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @GetMapping("/archived")
    public List<ConversationResponse> findArchived(
            @PathVariable UUID workspaceId
    ) {
        return conversationService.findArchived(workspaceId)
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @GetMapping("/{conversationId}")
    public ConversationResponse findById(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId
    ) {
        Conversation conversation =
                conversationService.findById(
                        workspaceId,
                        conversationId
                );

        return ConversationResponse.from(conversation);
    }

    @PatchMapping("/{conversationId}")
    public ConversationResponse update(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdateConversationRequest request
    ) {
        Conversation conversation =
                conversationService.update(
                        workspaceId,
                        conversationId,
                        request.title()
                );

        return ConversationResponse.from(conversation);
    }

    @PostMapping("/{conversationId}/pin")
    public ConversationResponse pin(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId
    ) {
        Conversation conversation =
                conversationService.pin(
                        workspaceId,
                        conversationId
                );

        return ConversationResponse.from(conversation);
    }

    @PostMapping("/{conversationId}/unpin")
    public ConversationResponse unpin(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId
    ) {
        Conversation conversation =
                conversationService.unpin(
                        workspaceId,
                        conversationId
                );

        return ConversationResponse.from(conversation);
    }

    @PostMapping("/{conversationId}/archive")
    public ConversationResponse archive(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId
    ) {
        Conversation conversation =
                conversationService.archive(
                        workspaceId,
                        conversationId
                );

        return ConversationResponse.from(conversation);
    }

    @PostMapping("/{conversationId}/restore")
    public ConversationResponse restore(
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId
    ) {
        Conversation conversation =
                conversationService.restore(
                        workspaceId,
                        conversationId
                );

        return ConversationResponse.from(conversation);
    }
}