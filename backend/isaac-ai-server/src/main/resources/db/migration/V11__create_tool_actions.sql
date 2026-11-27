CREATE TABLE assistant_tasks (
 id UUID PRIMARY KEY,
 workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
 title VARCHAR(300) NOT NULL,
 completed BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE tool_actions (
 id UUID PRIMARY KEY,
 workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
 tool VARCHAR(40) NOT NULL,
 argument VARCHAR(300) NOT NULL,
 status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','EXECUTED','REJECTED')),
 result TEXT,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX assistant_tasks_workspace_idx ON assistant_tasks(workspace_id);
CREATE INDEX tool_actions_workspace_idx ON tool_actions(workspace_id, created_at);
