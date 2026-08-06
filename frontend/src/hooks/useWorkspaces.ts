import { useCallback, useEffect, useState } from "react";

import { createWorkspace, getWorkspaces } from "../services/workspaceApi";
import type { CreateWorkspaceRequest, Workspace } from "../types/workspace";

interface UseWorkspacesResult {
  workspaces: Workspace[];
  selectedWorkspace: Workspace | null;
  selectWorkspace: (workspace: Workspace) => void;
  createNewWorkspace: (request: CreateWorkspaceRequest) => Promise<void>;
  refreshWorkspaces: () => Promise<void>;
  isLoading: boolean;
  isCreating: boolean;
  error: string;
}

export function useWorkspaces(): UseWorkspacesResult {
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);

  const [selectedWorkspace, setSelectedWorkspace] = useState<Workspace | null>(
    null,
  );

  const [isLoading, setIsLoading] = useState(false);

  const [isCreating, setIsCreating] = useState(false);

  const [error, setError] = useState("");

  const refreshWorkspaces = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    setError("");

    try {
      const loadedWorkspaces = await getWorkspaces();

      setWorkspaces(loadedWorkspaces);

      setSelectedWorkspace((currentSelection) => {
        if (!currentSelection) {
          return loadedWorkspaces[0] ?? null;
        }

        return (
          loadedWorkspaces.find(
            (workspace) => workspace.id === currentSelection.id,
          ) ??
          loadedWorkspaces[0] ??
          null
        );
      });
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load workspaces.",
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void refreshWorkspaces();
  }, [refreshWorkspaces]);

  async function createNewWorkspace(
    request: CreateWorkspaceRequest,
  ): Promise<void> {
    if (isCreating) {
      return;
    }

    setIsCreating(true);
    setError("");

    try {
      const workspace = await createWorkspace(request);

      setWorkspaces((currentWorkspaces) => [workspace, ...currentWorkspaces]);

      setSelectedWorkspace(workspace);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to create a workspace.",
      );
    } finally {
      setIsCreating(false);
    }
  }

  return {
    workspaces,
    selectedWorkspace,
    selectWorkspace: setSelectedWorkspace,
    createNewWorkspace,
    refreshWorkspaces,
    isLoading,
    isCreating,
    error,
  };
}
