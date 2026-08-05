import { useCallback, useEffect, useState } from "react";

import {
  createConversation,
  getActiveConversations,
} from "../services/conversationApi";
import type { Conversation } from "../types/conversation";

interface UseConversationsResult {
  conversations: Conversation[];
  selectedConversation: Conversation | null;
  selectConversation: (conversation: Conversation) => void;
  createNewConversation: () => Promise<void>;
  refreshConversations: () => Promise<void>;
  isLoading: boolean;
  isCreating: boolean;
  error: string;
}

export function useConversations(workspaceId: string): UseConversationsResult {
  const [conversations, setConversations] = useState<Conversation[]>([]);

  const [selectedConversation, setSelectedConversation] =
    useState<Conversation | null>(null);

  const [isLoading, setIsLoading] = useState(false);

  const [isCreating, setIsCreating] = useState(false);

  const [error, setError] = useState("");

  const refreshConversations = useCallback(async (): Promise<void> => {
    if (!workspaceId) {
      setConversations([]);
      setSelectedConversation(null);
      setError("");
      return;
    }

    setIsLoading(true);
    setError("");

    try {
      const loadedConversations = await getActiveConversations(workspaceId);

      setConversations(loadedConversations);

      setSelectedConversation((currentSelection) =>
        findSelectedConversation(loadedConversations, currentSelection),
      );
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Unable to load conversations."));
    } finally {
      setIsLoading(false);
    }
  }, [workspaceId]);

  useEffect(() => {
    void refreshConversations();
  }, [refreshConversations]);

  async function createNewConversation(): Promise<void> {
    if (!workspaceId || isCreating) {
      return;
    }

    setIsCreating(true);
    setError("");

    try {
      const newConversation = await createConversation(workspaceId, {
        title: "New Conversation",
      });

      setConversations((currentConversations) => [
        newConversation,
        ...currentConversations,
      ]);

      setSelectedConversation(newConversation);
    } catch (requestError) {
      setError(
        getErrorMessage(requestError, "Unable to create a conversation."),
      );
    } finally {
      setIsCreating(false);
    }
  }

  return {
    conversations,
    selectedConversation,
    selectConversation: setSelectedConversation,
    createNewConversation,
    refreshConversations,
    isLoading,
    isCreating,
    error,
  };
}

function findSelectedConversation(
  conversations: Conversation[],
  currentSelection: Conversation | null,
): Conversation | null {
  if (!currentSelection) {
    return conversations[0] ?? null;
  }

  return (
    conversations.find(
      (conversation) => conversation.id === currentSelection.id,
    ) ??
    conversations[0] ??
    null
  );
}

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  return error instanceof Error ? error.message : fallbackMessage;
}
