export interface Workspace {
  id: string;
  name: string;
  description: string;
  systemPrompt: string;
  color: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWorkspaceRequest {
  name: string;
  description: string;
  systemPrompt: string;
  color: string;
}
