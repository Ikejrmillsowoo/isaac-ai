import { useEffect, useState } from "react";
import {
  createMemory, deleteMemory, fetchMemories, memoryCategories,
} from "../../services/memoryApi";
import type { MemoryCategory, WorkspaceMemory } from "../../services/memoryApi";
import type { FormEvent } from "react";
import "./MemoryPanel.css";

interface Props {
  workspaceId: string;
  chatBusy: boolean;
}

export default function MemoryPanel({ workspaceId, chatBusy }: Props) {
  const [memories, setMemories] = useState<WorkspaceMemory[]>([]);
  const [content, setContent] = useState("");
  const [category, setCategory] = useState<MemoryCategory>("CONTEXT");
  const [filter, setFilter] = useState("ALL");
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    if (chatBusy || busy) return;
    const controller = new AbortController();
    fetchMemories(workspaceId, controller.signal)
      .then((data) => {
        if (!controller.signal.aborted) {
          setMemories(data);
          setError("");
        }
      })
      .catch((err: unknown) => {
        if (!controller.signal.aborted) {
          setError(err instanceof Error ? err.message : "Unable to load memories.");
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [workspaceId, revision, chatBusy, busy]);

  async function add(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!content.trim() || busy || chatBusy) return;
    setBusy(true);
    setError("");
    try {
      const saved = await createMemory(workspaceId, content.trim(), category);
      setMemories((current) => [saved, ...current.filter((m) => m.id !== saved.id)]);
      setContent("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to save memory.");
    } finally {
      setBusy(false);
    }
  }

  async function remove(memory: WorkspaceMemory) {
    if (busy || chatBusy || !window.confirm("Delete this saved memory?")) return;
    setBusy(true);
    setError("");
    try {
      await deleteMemory(workspaceId, memory.id);
      setMemories((current) => current.filter((m) => m.id !== memory.id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to delete memory.");
    } finally {
      setBusy(false);
    }
  }

  const visible = memories.filter((m) =>
    (filter === "ALL" || m.category === filter) &&
    m.content.toLowerCase().includes(query.trim().toLowerCase()),
  );

  return (
    <details className="memory-panel">
      <summary>Memory ({memories.length})</summary>
      <p className="memory-hint">Saved information for this workspace.</p>
      <form onSubmit={add}>
        <label>
          New memory
          <textarea value={content} onChange={(e) => setContent(e.target.value)} required rows={3} />
        </label>
        <label>
          Category
          <select value={category} onChange={(e) => setCategory(e.target.value as MemoryCategory)}>
            {memoryCategories.map((item) => <option key={item}>{item}</option>)}
          </select>
        </label>
        <button disabled={busy || chatBusy || !content.trim()} type="submit">Save memory</button>
      </form>
      <label>
        Search memories
        <input type="search" value={query} onChange={(e) => setQuery(e.target.value)} />
      </label>
      <label>
        Filter category
        <select value={filter} onChange={(e) => setFilter(e.target.value)}>
          <option value="ALL">All categories</option>
          {memoryCategories.map((item) => <option key={item}>{item}</option>)}
        </select>
      </label>
      <button type="button" disabled={busy || chatBusy} onClick={() => { setLoading(true); setRevision((n) => n + 1); }}>Refresh</button>
      {error && <p role="alert" className="memory-error">{error}</p>}
      {loading ? <p role="status">Loading memories…</p> : (
        visible.length === 0 ? <p>No matching memories.</p> : (
          <ul className="memory-list">
            {visible.map((memory) => (
              <li key={memory.id}>
                <small>{memory.category}</small>
                <p>{memory.content}</p>
                <button type="button" disabled={busy || chatBusy} onClick={() => void remove(memory)}>Delete</button>
              </li>
            ))}
          </ul>
        )
      )}
    </details>
  );
}
