import type {
  ChatRequest,
  ChatResponse,
  StoredMessageResponse,
} from "../types/chat";

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

export async function streamChatMessage(
  request: ChatRequest,
  onChunk: (chunk: string) => void,
): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/chat/stream`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "text/plain",
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response));
  }

  if (!response.body) {
    throw new Error("The streaming response did not contain a body.");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { value, done } = await reader.read();

    if (done) {
      const remainingText = decoder.decode();

      if (remainingText) {
        onChunk(remainingText);
      }

      break;
    }

    const chunk = decoder.decode(value, {
      stream: true,
    });

    if (chunk) {
      onChunk(chunk);
    }
  }
}

export async function getConversationMessages(
  workspaceId: string,
  conversationId: string,
): Promise<StoredMessageResponse[]> {
  const response = await fetch(
    `${API_BASE_URL}/api/workspaces/${workspaceId}/conversations/${conversationId}/messages`,
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

  return (await response.json()) as StoredMessageResponse[];
}
