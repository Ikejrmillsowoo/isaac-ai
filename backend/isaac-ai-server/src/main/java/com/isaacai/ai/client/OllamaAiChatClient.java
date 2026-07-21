package com.isaacai.ai.client;

import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OllamaAiChatClient implements AiChatClient {

    private final ChatClient chatClient;

    public OllamaAiChatClient(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                        You are Isaac AI, a private personal assistant.
                        Be clear, practical, and accurate.
                        Do not invent facts.
                        """)
                .build();
    }

    @Override
    public String chat(List<Message> history) {

        List<org.springframework.ai.chat.messages.Message> aiMessages =
                history.stream()
                        .map(this::toAiMessage)
                        .toList();

        return chatClient
                .prompt()
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