package com.isaacai.chat.service;

import com.isaacai.ai.client.AiChatClient;
import com.isaacai.chat.dto.ChatRequest;
import com.isaacai.chat.dto.ChatResponse;
import com.isaacai.server.message.model.Message;
import org.springframework.stereotype.Service;
import com.isaacai.title.AiTitleGenerator;
import com.isaacai.server.conversation.model.Conversation;
import com.isaacai.server.conversation.service.ConversationService;

@Service
public class ChatService {

    private final AiChatClient aiChatClient;
    private final ChatSessionService chatSessionService;
    private final AiTitleGenerator aiTitleGenerator;
    private final ConversationService conversationService;




    public ChatService(
            AiChatClient aiChatClient,
            ChatSessionService chatSessionService,
                AiTitleGenerator aiTitleGenerator,
                ConversationService conversationService

    ) {
        this.aiChatClient = aiChatClient;
        this.chatSessionService = chatSessionService;
        this.aiTitleGenerator = aiTitleGenerator;
        this.conversationService = conversationService;
    }

    public ChatResponse chat(ChatRequest request) {

        PreparedChat preparedChat =
                chatSessionService.prepareConversation(request);

        String answer =
                aiChatClient.chat(preparedChat.history());

        Message assistantMessage =
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
        

//         if (shouldGenerateTitle(request.conversationId())) {

//     String generatedTitle =
//             aiTitleGenerator.generateTitle(
//                     request.message()
//             );

//     conversationService.update(
//             request.workspaceId(),
//             request.conversationId(),
//             generatedTitle
//     );
// }

        return new ChatResponse(
                request.conversationId(),
                preparedChat.userMessage().getId(),
                assistantMessage.getId(),
                answer
        );
    }
}