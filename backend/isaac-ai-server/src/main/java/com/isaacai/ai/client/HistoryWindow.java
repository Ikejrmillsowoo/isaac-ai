package com.isaacai.ai.client;

import com.isaacai.server.message.model.Message;
import com.isaacai.server.message.model.MessageRole;
import java.util.List;

public final class HistoryWindow {
    private HistoryWindow() {}

    public static List<Message> select(List<Message> history, String systemPrompt) {
        if (history.isEmpty()) return List.of();
        int remaining = Math.max(0, 10000 - (systemPrompt == null ? 0 : systemPrompt.length()));
        int start = history.size() - 1;
        remaining -= history.get(start).getContent().length() + 32;
        while (start > 0 && history.size() - start < 20) {
            int cost = history.get(start - 1).getContent().length() + 32;
            if (cost > remaining) break;
            remaining -= cost;
            start--;
        }
        while (start < history.size() - 1 && history.get(start).getRole() == MessageRole.ASSISTANT) start++;
        return List.copyOf(history.subList(start, history.size()));
    }
}
