import type { Workspace } from "../../types/workspace";

interface WorkspaceSelectorProps {
  workspaces: Workspace[];
  selectedWorkspaceId?: string;
  isLoading: boolean;
  error: string;
  onSelect: (workspace: Workspace) => void;
}

export function WorkspaceSelector({
  workspaces,
  selectedWorkspaceId,
  isLoading,
  error,
  onSelect,
}: WorkspaceSelectorProps) {
  return (
    <section className="workspace-selector">
      <label htmlFor="workspace-select">Workspace</label>

      <select
        id="workspace-select"
        value={selectedWorkspaceId ?? ""}
        onChange={(event) => {
          const workspace = workspaces.find(
            (item) => item.id === event.target.value,
          );

          if (workspace) {
            onSelect(workspace);
          }
        }}
        disabled={isLoading || workspaces.length === 0}
      >
        {workspaces.length === 0 && <option value="">No workspaces</option>}

        {workspaces.map((workspace) => (
          <option key={workspace.id} value={workspace.id}>
            {workspace.name}
          </option>
        ))}
      </select>

      {error && (
        <p className="sidebar-error" role="alert">
          {error}
        </p>
      )}
    </section>
  );
}
