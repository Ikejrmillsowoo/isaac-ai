export type ChatRole = "user" | "assistant";

export interface ChatMessage {
  id: string;
  role: ChatRole;
  content: string;
}

export interface StoredMessageResponse {
  id: string;
  conversationId: string;
  role: "USER" | "ASSISTANT";
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatRequest {
  workspaceId: string;
  conversationId: string;
  message: string;
}

export interface ChatResponse {
  conversationId: string;
  userMessageId: string;
  assistantMessageId: string;
  answer: string;
}
