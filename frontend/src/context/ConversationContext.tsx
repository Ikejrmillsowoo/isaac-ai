import { createContext, useContext, type ReactNode } from "react";

import { useConversations } from "../hooks/useConversations";
import type { Conversation } from "../types/conversation";

interface ConversationContextValue {
  conversations: Conversation[];
  selectedConversation: Conversation | null;
  selectConversation: (conversation: Conversation) => void;
  createNewConversation: () => Promise<void>;
  refreshConversations: () => Promise<void>;
  isLoading: boolean;
  isCreating: boolean;
  error: string;
}

const ConversationContext = createContext<ConversationContextValue | null>(
  null,
);

interface ConversationProviderProps {
  workspaceId: string;
  children: ReactNode;
}

export function ConversationProvider({
  workspaceId,
  children,
}: ConversationProviderProps) {
  const value = useConversations(workspaceId);

  return (
    <ConversationContext.Provider value={value}>
      {children}
    </ConversationContext.Provider>
  );
}

export function useConversationContext(): ConversationContextValue {
  const context = useContext(ConversationContext);

  if (!context) {
    throw new Error(
      "useConversationContext must be used within ConversationProvider.",
    );
  }

  return context;
}
