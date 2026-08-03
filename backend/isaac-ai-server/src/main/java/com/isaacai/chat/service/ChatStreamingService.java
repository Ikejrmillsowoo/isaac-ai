package com.isaacai.chat.service;

import com.isaacai.ai.client.AiStreamingClient;
import com.isaacai.title.AiTitleGenerator;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import com.isaacai.server.workspace.service.WorkspaceService;

@Service
public class ChatStreamingService {

    private final AiStreamingClient aiStreamingClient;
    private final ChatSessionService chatSessionService;
    private final AiTitleGenerator aiTitleGenerator;
    private final ConversationService conversationService;
    private final WorkspaceService workspaceService;
    

    private static final String DEFAULT_SYSTEM_PROMPT = """
        You are Isaac AI, a private personal assistant.
        Be clear, practical, and accurate.
        Do not invent facts.
        """;

    public ChatStreamingService(
            AiStreamingClient aiStreamingClient,
            ChatSessionService chatSessionService,
            AiTitleGenerator aiTitleGenerator,
            ConversationService conversationService,
            WorkspaceService workspaceService
           
    ) {
        this.aiStreamingClient = aiStreamingClient;
        this.chatSessionService = chatSessionService;
        this.aiTitleGenerator = aiTitleGenerator;
        this.conversationService = conversationService;
        this.workspaceService = workspaceService;
       
    }

    public Flux<String> stream(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

        String systemPrompt =
        resolveSystemPrompt(
                workspaceService.findById(
                        request.workspaceId()
                ).getSystemPrompt()
        );

        StringBuilder assistantResponse =
                new StringBuilder();

        return aiStreamingClient
                .stream( systemPrompt, preparedChat.history())
                .doOnNext(assistantResponse::append)
                .doOnComplete(() -> {
                    String answer = assistantResponse.toString();

                    if (answer.isBlank()) {
                        return;
                    }

                    chatSessionService.saveAssistantMessage(
                            request,
                            answer
                    );

                    Conversation conversation =
                            conversationService.findById(
                                    request.workspaceId(),
                                    request.conversationId()
                            );

                    if (conversation.hasDefaultTitle()) {
                        String generatedTitle =
                                aiTitleGenerator.generateTitle(
                                        request.message()
                                );

                        conversationService.rename(
                                request.workspaceId(),
                                request.conversationId(),
                                generatedTitle
                        );
                    }
                });
    }

    private String resolveSystemPrompt(String systemPrompt) {
    if (systemPrompt == null || systemPrompt.isBlank()) {
        return DEFAULT_SYSTEM_PROMPT;
    }

    return systemPrompt.trim();
}
}