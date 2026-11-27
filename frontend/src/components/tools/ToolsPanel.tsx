import { useEffect, useState } from "react";

type Task = { id: string; title: string; completed: boolean };
type Action = { id: string; tool: string; argument: string; status: string; result: string | null };
type Snapshot = { enabled: boolean; tasks: Task[]; history: Action[] };

export default function ToolsPanel({ workspaceId, chatBusy }: { workspaceId: string; chatBusy: boolean }) {
  const base = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080") + "/api/workspaces/" + workspaceId + "/tools";
  const [data, setData] = useState<Snapshot | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    if (chatBusy || busy) return;
    const controller = new AbortController();
    const get = async (path: string) => {
      const response = await fetch(base + path, { signal: controller.signal });
      if (!response.ok) throw new Error("Unable to load tools: " + response.status);
      return response.json();
    };
    void Promise.all([get("/permission"), get("/tasks"), get("/history")]).then(([permission, tasks, history]) => {
      if (!controller.signal.aborted) setData({ enabled: permission.enabled, tasks, history });
    }).catch((e: unknown) => { if (!controller.signal.aborted) setError(String(e)); });
    return () => controller.abort();
  }, [base, chatBusy, busy, revision]);
  async function mutate(path: string, method: string, body: object) {
    setBusy(true);
    setError("");
    try {
      const response = await fetch(base + path, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
      if (!response.ok) throw new Error("Action failed: " + response.status);
    } catch (e) { setError(String(e)); }
    finally { setBusy(false); }
  }
  const disabled = busy || chatBusy || !data;
  return <details open>
    <summary>Tools and actions</summary>
    <label><input type="checkbox" checked={data?.enabled ?? false} disabled={disabled}
      onChange={e => void mutate("/permission", "PUT", { enabled: e.target.checked })} /> Enable AI tools</label>
    <button disabled={busy || chatBusy} onClick={() => { setError(""); setRevision(r => r + 1); }}>Refresh</button>
    {error && <p role="alert">{error}</p>}
    <h4>Tasks</h4>
    {data?.tasks.length === 0 && <p>No tasks yet.</p>}
    {data?.tasks.map(task => <p key={task.id}>{task.completed ? "Completed: " : "Open: "}{task.title}</p>)}
    <h4>Action history</h4>
    {data?.history.map(action => <div key={action.id}>
      <p>{action.tool}: {action.argument} — {action.status}</p>
      {action.status === "PENDING" && <>
        <button disabled={disabled || !data.enabled} onClick={() => void mutate("/history/" + action.id + "/decision", "POST", { approve: true })}>Approve</button>
        <button disabled={disabled} onClick={() => void mutate("/history/" + action.id + "/decision", "POST", { approve: false })}>Reject</button>
      </>}
      {action.result && <p>{action.result}</p>}
    </div>)}
  </details>;
}
