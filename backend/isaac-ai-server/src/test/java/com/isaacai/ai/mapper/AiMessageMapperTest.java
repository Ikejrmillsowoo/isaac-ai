package com.isaacai.ai.mapper;

import com.isaacai.server.message.model.Message;
import com.isaacai.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;

import static org.assertj.core.api.Assertions.assertThat;

class AiMessageMapperTest {

    private final AiMessageMapper mapper = new AiMessageMapper();

    @Test
    void shouldMapUserMessage() {
        Message message =
                TestDataFactory.userMessage();

        org.springframework.ai.chat.messages.Message result =
                mapper.map(message);

        assertThat(result)
                .isInstanceOf(UserMessage.class);

        assertThat(result.getText())
                .isEqualTo(message.getContent());
    }

    @Test
    void shouldMapAssistantMessage() {
        Message message =
                TestDataFactory.assistantMessage();

        org.springframework.ai.chat.messages.Message result =
                mapper.map(message);

        assertThat(result)
                .isInstanceOf(AssistantMessage.class);

        assertThat(result.getText())
                .isEqualTo(message.getContent());
    }
}