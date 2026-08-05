export interface Conversation {
  id: string;
  workspaceId: string;
  title: string;
  pinned: boolean;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateConversationRequest {
  title: string;
}
