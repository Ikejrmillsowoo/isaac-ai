import type { ChatRequest, ChatResponse } from "../types/chat";

const CHAT_API_URL = "http://localhost:8080/api/chat";

export async function sendChatMessage(
  request: ChatRequest
): Promise<ChatResponse> {
  const response = await fetch(CHAT_API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const errorBody = await response.text();

    throw new Error(
      errorBody || `Chat request failed with status ${response.status}`
    );
  }

  return (await response.json()) as ChatResponse;
}