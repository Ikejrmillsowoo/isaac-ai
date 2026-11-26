import { createContext, useContext } from "react";
import type { CreateWorkspaceRequest, Workspace } from "../types/workspace";

interface WorkspaceContextValue {
  workspaces: Workspace[];
  selectedWorkspace: Workspace | null;
  selectWorkspace: (workspace: Workspace) => void;
  createNewWorkspace: (request: CreateWorkspaceRequest) => Promise<void>;
  refreshWorkspaces: () => Promise<void>;
  isLoading: boolean;
  isCreating: boolean;
  error: string;
}

export const WorkspaceContext = createContext<WorkspaceContextValue | null>(null);

export function useWorkspaceContext(): WorkspaceContextValue {
  const context = useContext(WorkspaceContext);

  if (!context) {
    throw new Error(
      "useWorkspaceContext must be used within WorkspaceProvider.",
    );
  }

  return context;
}
