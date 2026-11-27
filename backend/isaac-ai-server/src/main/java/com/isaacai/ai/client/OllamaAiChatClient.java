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
    private final com.isaacai.tools.TaskActions actions;
    private final AiMessageMapper aiMessageMapper;

    public OllamaAiChatClient(ChatClient.Builder builder, AiMessageMapper aiMessageMapper, com.isaacai.tools.TaskActions actions) {
        this.actions=actions;
        this.chatClient = builder.build();
        this.aiMessageMapper = aiMessageMapper;
    }

    @Override
    public String chat(String systemPrompt,List<Message> history) {

        List<org.springframework.ai.chat.messages.Message> aiMessages =
                HistoryWindow.select(history, systemPrompt).stream()
                        .map(this::toAiMessage)
                        .toList();

        return chatClient
                .prompt()
                .system(systemPrompt)
                .messages(aiMessages)
                .call()
                .content();
    }

    @Override
    public String chatWithTools(String systemPrompt,List<Message> history,java.util.UUID workspace) {
        if (!actions.enabled(workspace)) return chat(systemPrompt,history);
        String prompt=systemPrompt+"\nTools are available. Task changes are only proposals until approved in the Actions panel. Never claim a pending action was executed.";
        return chatClient.prompt().system(prompt)
            .messages(HistoryWindow.select(history,prompt).stream().map(this::toAiMessage).toList())
            .tools(new com.isaacai.tools.WorkspaceTools(workspace,actions)).call().content();
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