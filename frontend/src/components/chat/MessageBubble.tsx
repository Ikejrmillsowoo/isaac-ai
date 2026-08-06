import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

import type { ChatMessage } from "../../types/chat";

interface MessageBubbleProps {
  message: ChatMessage;
  isStreaming?: boolean;
}

export function MessageBubble({
  message,
  isStreaming = false,
}: MessageBubbleProps) {
  const sender = message.role === "user" ? "You" : "Isaac AI";

  return (
    <article className={`message message-${message.role}`}>
      <strong>{sender}</strong>

      {message.role === "assistant" ? (
        <div className="message-content markdown-content">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {message.content}
          </ReactMarkdown>

          {isStreaming && (
            <span
              className="streaming-cursor"
              aria-label="Isaac AI is responding"
            />
          )}
        </div>
      ) : (
        <p className="message-content">{message.content}</p>
      )}
    </article>
  );
}
