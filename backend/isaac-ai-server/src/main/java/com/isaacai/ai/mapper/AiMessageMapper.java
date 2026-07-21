package com.isaacai.ai.mapper;

import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

@Component
public class AiMessageMapper {

    public org.springframework.ai.chat.messages.Message map(
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