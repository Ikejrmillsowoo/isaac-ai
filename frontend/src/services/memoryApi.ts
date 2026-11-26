export const memoryCategories = [
  "PREFERENCE", "PERSONAL", "GOAL", "PROJECT", "DECISION", "CONTEXT",
] as const;

export type MemoryCategory = (typeof memoryCategories)[number];

export interface WorkspaceMemory {
  id: string;
  content: string;
  category: MemoryCategory;
  sourceConversationId: string | null;
  createdAt: string;
  updatedAt: string;
}

const baseUrl = "http://localhost:8080/api";

function memoryUrl(workspaceId: string) {
  return `${baseUrl}/workspaces/${encodeURIComponent(workspaceId)}/memories`;
}

async function check(response: Response) {
  if (!response.ok) throw new Error(`Memory request failed (${response.status}).`);
}

export async function fetchMemories(workspaceId: string, signal?: AbortSignal): Promise<WorkspaceMemory[]> {
  const response = await fetch(memoryUrl(workspaceId), { signal });
  await check(response);
  return response.json();
}

export async function createMemory(workspaceId: string, content: string, category: MemoryCategory): Promise<WorkspaceMemory> {
  const response = await fetch(memoryUrl(workspaceId), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ content, category }),
  });
  await check(response);
  return response.json();
}

export async function deleteMemory(workspaceId: string, memoryId: string): Promise<void> {
  const response = await fetch(`${memoryUrl(workspaceId)}/${encodeURIComponent(memoryId)}`, { method: "DELETE" });
  await check(response);
}
