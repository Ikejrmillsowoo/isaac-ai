import type { Conversation } from "../types/conversation";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function getActiveConversations(
  workspaceId: string,
): Promise<Conversation[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/workspaces/${workspaceId}/conversations`,
    {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
    },
  );

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response));
  }

  return (await response.json()) as Conversation[];
}

async function extractErrorMessage(response: Response): Promise<string> {
  const responseText = await response.text();

  if (!responseText) {
    return `Conversation request failed with status ${response.status}.`;
  }

  try {
    const errorBody = JSON.parse(responseText) as {
      message?: string;
    };

    return (
      errorBody.message ??
      `Conversation request failed with status ${response.status}.`
    );
  } catch {
    return responseText;
  }
}
