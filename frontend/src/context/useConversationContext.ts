import { createContext, useContext } from "react";
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

export const ConversationContext = createContext<ConversationContextValue | null>(
  null,
);

export function useConversationContext(): ConversationContextValue {
  const context = useContext(ConversationContext);

  if (!context) {
    throw new Error(
      "useConversationContext must be used within ConversationProvider.",
    );
  }

  return context;
}
