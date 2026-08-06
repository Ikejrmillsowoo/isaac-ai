import type { ChatMessage } from "../../types/chat";
import { MessageBubble } from "./MessageBubble";

interface ChatWindowProps {
  messages: ChatMessage[];
  isLoading: boolean;
}

export function ChatWindow({ messages, isLoading }: ChatWindowProps) {
  return (
    <section className="chat-window">
      {messages.length === 0 ? (
        <div className="empty-state">
          <h2>Welcome to Isaac AI</h2>

          <p>Ask a question to test your locally running assistant.</p>
        </div>
      ) : (
        <div className="message-list">
          {messages.map((message, index) => {
            const isLastMessage = index === messages.length - 1;

            const isStreaming =
              isLoading && isLastMessage && message.role === "assistant";

            return (
              <MessageBubble
                key={message.id}
                message={message}
                isStreaming={isStreaming}
              />
            );
          })}
        </div>
      )}
    </section>
  );
}
