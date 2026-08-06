import type { Workspace, CreateWorkspaceRequest } from "../types/workspace";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function getWorkspaces(): Promise<Workspace[]> {
  const response = await fetch(`${API_BASE_URL}/api/workspaces`);

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response));
  }

  return await response.json();
}

export async function createWorkspace(
  request: CreateWorkspaceRequest,
): Promise<Workspace> {
  const response = await fetch(`${API_BASE_URL}/api/workspaces`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response));
  }

  return await response.json();
}

async function extractErrorMessage(response: Response): Promise<string> {
  const text = await response.text();

  if (!text) {
    return `Workspace request failed (${response.status})`;
  }

  try {
    return JSON.parse(text).message;
  } catch {
    return text;
  }
}
