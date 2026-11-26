package com.isaacai.ai.client;
import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HistoryWindowTest {
    @Test void preservesLatestMessageEvenWhenOversized() {
        Message old = message("old", MessageRole.USER);
        Message latest = message("x".repeat(12000), MessageRole.USER);
        assertThat(HistoryWindow.select(List.of(old, latest), "")).containsExactly(latest);
    }
    @Test void preservesRecentHistoryInOrder() {
        Message first = message("first", MessageRole.USER);
        Message second = message("second", MessageRole.ASSISTANT);
        Message latest = message("latest", MessageRole.USER);
        assertThat(HistoryWindow.select(List.of(first, second, latest), "")).containsExactly(first, second, latest);
    }
    private Message message(String text, MessageRole role) {
        Message m = mock(Message.class);
        when(m.getContent()).thenReturn(text);
        when(m.getRole()).thenReturn(role);
        return m;
    }
}
