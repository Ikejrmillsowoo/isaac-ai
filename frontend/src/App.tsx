import { useState } from "react";
import type { SyntheticEvent } from "react";
import { sendChatMessage } from "./services/chatApi";
import type { ChatMessage } from "./types/chat";
import "./App.css";

const WORKSPACE_ID = "4aa0a5a1-be58-48a3-977a-97a2e5b74bf6";

const CONVERSATION_ID = "e889b7a2-94a2-41fa-a72e-88b629861e71";

function App() {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(
    event: SyntheticEvent<HTMLFormElement>,
  ): Promise<void> {
    event.preventDefault();

    const messageText = input.trim();

    if (!messageText || isLoading) {
      return;
    }

    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: "user",
      content: messageText,
    };

    setMessages((currentMessages) => [...currentMessages, userMessage]);

    setInput("");
    setError("");
    setIsLoading(true);

    try {
      const response = await sendChatMessage({
        workspaceId: WORKSPACE_ID,
        conversationId: CONVERSATION_ID,
        message: messageText,
      });

      const assistantMessage: ChatMessage = {
        id: response.assistantMessageId,
        role: "assistant",
        content: response.answer,
      };

      setMessages((currentMessages) => [...currentMessages, assistantMessage]);
    } catch (requestError) {
      const errorMessage =
        requestError instanceof Error
          ? requestError.message
          : "Isaac AI could not complete the request.";

      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <main className="app">
      <header className="header">
        <div>
          <p className="eyebrow">Private local assistant</p>
          <h1>Isaac AI</h1>
        </div>

        <div className="status">
          <span className={`status-dot ${isLoading ? "working" : "ready"}`} />

          {isLoading ? "Thinking" : "Ready"}
        </div>
      </header>

      <section className="chat-window">
        {messages.length === 0 ? (
          <div className="empty-state">
            <h2>Welcome to Isaac AI</h2>

            <p>Ask a question to test your locally running assistant.</p>
          </div>
        ) : (
          <div className="message-list">
            {messages.map((message) => (
              <article
                key={message.id}
                className={`message message-${message.role}`}
              >
                <strong>{message.role === "user" ? "You" : "Isaac AI"}</strong>

                <p>{message.content}</p>
              </article>
            ))}

            {isLoading && (
              <article className="message message-assistant">
                <strong>Isaac AI</strong>
                <p>Thinking…</p>
              </article>
            )}
          </div>
        )}
      </section>

      {error && (
        <div className="error-message" role="alert">
          {error}
        </div>
      )}

      <form className="composer" onSubmit={handleSubmit}>
        <label htmlFor="chat-input" className="sr-only">
          Message
        </label>

        <textarea
          id="chat-input"
          value={input}
          onChange={(event) => setInput(event.target.value)}
          placeholder="Ask Isaac AI something..."
          rows={3}
          disabled={isLoading}
        />

        <button type="submit" disabled={isLoading || !input.trim()}>
          {isLoading ? "Thinking…" : "Send"}
        </button>
      </form>
    </main>
  );
}

export default App;
