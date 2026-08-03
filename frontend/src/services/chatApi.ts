import type { ChatRequest, ChatResponse } from "../types/chat";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function sendChatMessage(
  request: ChatRequest,
): Promise<ChatResponse> {
  console.log("Sending chat request:", request);
  console.log("Sending chat JSON:", JSON.stringify(request));

  const response = await fetch(`${API_BASE_URL}/api/chat`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const errorMessage = await extractErrorMessage(response);

    throw new Error(errorMessage);
  }

  return (await response.json()) as ChatResponse;
}

async function extractErrorMessage(response: Response): Promise<string> {
  const rawBody = await response.text();

  console.error("Chat error response:", response.status, rawBody);

  if (!rawBody) {
    return `Chat request failed with status ${response.status}.`;
  }

  try {
    const body = JSON.parse(rawBody) as {
      message?: string;
    };

    return (
      body.message ?? `Chat request failed with status ${response.status}.`
    );
  } catch {
    return rawBody;
  }
}
