package com.isaacai.chat.service;

import com.isaacai.ai.client.AiStreamingClient;
import com.isaacai.ai.retrieval.KnowledgeContextBuilder;
import com.isaacai.ai.retrieval.KnowledgeRetriever;
import com.isaacai.ai.retrieval.RetrievedKnowledge;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;
import com.isaacai.server.workspace.service.WorkspaceService;
import com.isaacai.title.AiTitleGenerator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatStreamingService {

//     private static final String DEFAULT_SYSTEM_PROMPT = """
//             You are Isaac AI, a private personal assistant.
//             Be clear, practical, and accurate.
//             Do not invent facts.
//             """;

    private final AiStreamingClient aiStreamingClient;
    private final ChatSessionService chatSessionService;
    private final AiTitleGenerator aiTitleGenerator;
    private final ConversationService conversationService;
    private final WorkspaceService workspaceService;
    private final KnowledgeRetriever knowledgeRetriever;
    private final KnowledgeContextBuilder knowledgeContextBuilder;
    private final ChatPromptBuilder chatPromptBuilder;

    public ChatStreamingService(
            AiStreamingClient aiStreamingClient,
            ChatSessionService chatSessionService,
            AiTitleGenerator aiTitleGenerator,
            ConversationService conversationService,
            WorkspaceService workspaceService,
            KnowledgeRetriever knowledgeRetriever,
            KnowledgeContextBuilder knowledgeContextBuilder,
            ChatPromptBuilder chatPromptBuilder
    ) {
        this.aiStreamingClient = aiStreamingClient;
        this.chatSessionService = chatSessionService;
        this.aiTitleGenerator = aiTitleGenerator;
        this.conversationService = conversationService;
        this.workspaceService = workspaceService;
        this.knowledgeRetriever = knowledgeRetriever;
        this.knowledgeContextBuilder = knowledgeContextBuilder;
        this.chatPromptBuilder = chatPromptBuilder;
    }

    public Flux<String> stream(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

       String workspaceSystemPrompt =
        workspaceService.findById(
                request.workspaceId()
        ).getSystemPrompt();

        // Retrieve relevant knowledge for this workspace
        List<RetrievedKnowledge> retrievedKnowledge =
                knowledgeRetriever.retrieve(
                        request.workspaceId(),
                        request.message()
                );

        // Convert retrieved chunks into context
        String knowledgeContext =
                knowledgeContextBuilder.build(
                        retrievedKnowledge
                );

        // Combine the workspace prompt and knowledge
       String augmentedSystemPrompt =
        chatPromptBuilder.build(
                workspaceSystemPrompt,
                knowledgeContext,
                ""
        );

        StringBuilder assistantResponse =
                new StringBuilder();

        return aiStreamingClient
                .stream(
                        augmentedSystemPrompt,
                        preparedChat.history()
                )
                .doOnNext(assistantResponse::append)
                .doOnComplete(() -> {

                    String answer =
                            assistantResponse.toString();

                    if (answer.isBlank()) {
                        return;
                    }

                    chatSessionService
                            .saveAssistantMessage(
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

//     private String resolveSystemPrompt(
//             String systemPrompt
//     ) {

//         if (systemPrompt == null ||
//                 systemPrompt.isBlank()) {
//             return DEFAULT_SYSTEM_PROMPT;
//         }

//         return systemPrompt.trim();
//     }

//     private String buildSystemPrompt(
//             String systemPrompt,
//             String knowledgeContext
//     ) {

//         if (knowledgeContext == null ||
//                 knowledgeContext.isBlank()) {
//             return systemPrompt;
//         }

//         return """
//                 %s

//                 You have access to relevant knowledge retrieved
//                 from documents in this workspace.

//                 Use the workspace knowledge below when it is
//                 relevant to the user's question.

//                 Do not claim that the knowledge says something
//                 that is not supported by the provided text.

//                 If the provided knowledge does not contain enough
//                 information to answer the question, say so rather
//                 than inventing information.

//                 --- WORKSPACE KNOWLEDGE ---

//                 %s

//                 --- END WORKSPACE KNOWLEDGE ---
//                 """.formatted(
//                 systemPrompt,
//                 knowledgeContext
//         );
//     }
}