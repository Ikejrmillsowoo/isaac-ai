import { useState } from "react";
import type { SyntheticEvent } from "react";

import { sendChatMessage } from "../services/chatApi";
import type { ChatMessage } from "../types/chat";

interface UseChatOptions {
  workspaceId: string;
  conversationId: string;
}

export function useChat({ workspaceId, conversationId }: UseChatOptions) {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(
    event: SyntheticEvent<HTMLFormElement>,
  ): Promise<void> {
    event.preventDefault();
    await sendMessage();
  }

  async function sendMessage(): Promise<void> {
    const messageText = input.trim();

    if (!messageText || isLoading) {
      return;
    }

    const temporaryUserMessageId = crypto.randomUUID();

    const userMessage: ChatMessage = {
      id: temporaryUserMessageId,
      role: "user",
      content: messageText,
    };

    setMessages((currentMessages) => [...currentMessages, userMessage]);

    setInput("");
    setError("");
    setIsLoading(true);

    try {
      const response = await sendChatMessage({
        workspaceId,
        conversationId,
        message: messageText,
      });

      const assistantMessage: ChatMessage = {
        id: response.assistantMessageId,
        role: "assistant",
        content: response.answer,
      };

      setMessages((currentMessages) => [
        ...currentMessages.map((message) =>
          message.id === temporaryUserMessageId
            ? {
                ...message,
                id: response.userMessageId,
              }
            : message,
        ),
        assistantMessage,
      ]);
    } catch (requestError) {
      const errorMessage =
        requestError instanceof Error
          ? requestError.message
          : "Isaac AI could not complete the request.";

      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }

  return {
    input,
    setInput,
    messages,
    isLoading,
    error,
    handleSubmit,
  };
}
