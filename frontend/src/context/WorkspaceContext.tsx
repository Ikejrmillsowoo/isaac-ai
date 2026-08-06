import { createContext, useContext, type ReactNode } from "react";

import { useWorkspaces } from "../hooks/useWorkspaces";
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

const WorkspaceContext = createContext<WorkspaceContextValue | null>(null);

interface WorkspaceProviderProps {
  children: ReactNode;
}

export function WorkspaceProvider({ children }: WorkspaceProviderProps) {
  const value = useWorkspaces();

  return (
    <WorkspaceContext.Provider value={value}>
      {children}
    </WorkspaceContext.Provider>
  );
}

export function useWorkspaceContext(): WorkspaceContextValue {
  const context = useContext(WorkspaceContext);

  if (!context) {
    throw new Error(
      "useWorkspaceContext must be used within WorkspaceProvider.",
    );
  }

  return context;
}
