import type { ReactNode } from "react";
import { WorkspaceContext } from "./useWorkspaceContext";
import { useWorkspaces } from "../hooks/useWorkspaces";

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

