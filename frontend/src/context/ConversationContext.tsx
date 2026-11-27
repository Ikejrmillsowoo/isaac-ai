import type { ReactNode } from "react";
import { ConversationContext } from "./useConversationContext";
import { useConversations } from "../hooks/useConversations";

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

