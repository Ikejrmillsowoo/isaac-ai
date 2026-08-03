package com.isaacai.ai.client;

import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import com.isaacai.ai.mapper.AiMessageMapper;

import java.util.List;

@Component
public class OllamaAiChatClient implements AiChatClient {

    private final ChatClient chatClient;
    private final AiMessageMapper aiMessageMapper;

    public OllamaAiChatClient(ChatClient.Builder builder, AiMessageMapper aiMessageMapper) {
        this.chatClient = builder.build();
        this.aiMessageMapper = aiMessageMapper;
    }

    @Override
    public String chat(String systemPrompt,List<Message> history) {

        List<org.springframework.ai.chat.messages.Message> aiMessages =
                history.stream()
                        .map(this::toAiMessage)
                        .toList();

        return chatClient
                .prompt()
                .system(systemPrompt)
                .messages(aiMessages)
                .call()
                .content();
    }

    private org.springframework.ai.chat.messages.Message toAiMessage(
            Message message
    ) {
        if (message.getRole() == MessageRole.USER) {
            return new UserMessage(message.getContent());
        }

        if (message.getRole() == MessageRole.ASSISTANT) {
            return new AssistantMessage(message.getContent());
        }

        throw new IllegalArgumentException(
                "Unsupported message role: " + message.getRole()
        );
    }
}