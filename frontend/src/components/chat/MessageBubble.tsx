import type { ChatMessage } from "../../types/chat";

interface MessageBubbleProps {
  message: ChatMessage;
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const sender = message.role === "user" ? "You" : "Isaac AI";

  return (
    <article className={`message message-${message.role}`}>
      <strong>{sender}</strong>
      <p>{message.content}</p>
    </article>
  );
}
