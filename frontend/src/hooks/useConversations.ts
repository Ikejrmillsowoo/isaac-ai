import { useEffect, useState } from "react";

import { getActiveConversations } from "../services/conversationApi";
import type { Conversation } from "../types/conversation";

interface UseConversationsResult {
  conversations: Conversation[];
  selectedConversation: Conversation | null;
  selectConversation: (conversation: Conversation) => void;
  isLoading: boolean;
  error: string;
}

export function useConversations(
  workspaceId: string,
  initialConversationId?: string,
): UseConversationsResult {
  const [conversations, setConversations] = useState<Conversation[]>([]);

  const [selectedConversation, setSelectedConversation] =
    useState<Conversation | null>(null);

  const [isLoading, setIsLoading] = useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    let isCancelled = false;

    async function loadConversations(): Promise<void> {
      setIsLoading(true);
      setError("");

      try {
        const loadedConversations = await getActiveConversations(workspaceId);

        if (isCancelled) {
          return;
        }

        setConversations(loadedConversations);

        const initialConversation =
          loadedConversations.find(
            (conversation) => conversation.id === initialConversationId,
          ) ??
          loadedConversations[0] ??
          null;

        setSelectedConversation(initialConversation);
      } catch (requestError) {
        if (isCancelled) {
          return;
        }

        const errorMessage =
          requestError instanceof Error
            ? requestError.message
            : "Unable to load conversations.";

        setError(errorMessage);
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadConversations();

    return () => {
      isCancelled = true;
    };
  }, [workspaceId, initialConversationId]);

  return {
    conversations,
    selectedConversation,
    selectConversation: setSelectedConversation,
    isLoading,
    error,
  };
}
