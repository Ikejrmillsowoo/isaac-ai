import { useState, useEffect } from "react";
import type { SyntheticEvent } from "react";
import { useConversationContext } from "../context/ConversationContext";

import {
  streamChatMessage,
  getConversationMessages,
} from "../services/chatApi";
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

  const { refreshConversations } = useConversationContext();

  useEffect(() => {
    let isCancelled = false;

    async function loadMessages(): Promise<void> {
      if (!workspaceId || !conversationId) {
        setMessages([]);
        return;
      }

      setIsLoading(true);
      setError("");
      setMessages([]);

      try {
        const storedMessages = await getConversationMessages(
          workspaceId,
          conversationId,
        );

        if (isCancelled) {
          return;
        }

        const mappedMessages: ChatMessage[] = storedMessages.map((message) => ({
          id: message.id,
          role: message.role === "USER" ? "user" : "assistant",
          content: message.content,
        }));

        setMessages(mappedMessages);
      } catch (requestError) {
        if (isCancelled) {
          return;
        }

        const errorMessage =
          requestError instanceof Error
            ? requestError.message
            : "Unable to load conversation messages.";

        setError(errorMessage);
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadMessages();

    return () => {
      isCancelled = true;
    };
  }, [workspaceId, conversationId]);

  async function handleSubmit(
    event: SyntheticEvent<HTMLFormElement>,
  ): Promise<void> {
    event.preventDefault();
    await sendMessage();
  }

  async function sendMessage(): Promise<void> {
    const messageText = input.trim();

    if (!messageText || isLoading || !workspaceId || !conversationId) {
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
      const temporaryAssistantMessageId = crypto.randomUUID();

      const assistantMessage: ChatMessage = {
        id: temporaryAssistantMessageId,
        role: "assistant",
        content: "",
      };

      setMessages((currentMessages) => [...currentMessages, assistantMessage]);

      await streamChatMessage(
        {
          workspaceId,
          conversationId,
          message: messageText,
        },
        (chunk) => {
          setMessages((currentMessages) =>
            currentMessages.map((message) =>
              message.id === temporaryAssistantMessageId
                ? {
                    ...message,
                    content: message.content + chunk,
                  }
                : message,
            ),
          );
        },
      );

      await refreshConversations();
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
