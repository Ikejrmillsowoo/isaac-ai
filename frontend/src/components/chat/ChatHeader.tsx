interface ChatHeaderProps {
  isLoading: boolean;
}

export function ChatHeader({ isLoading }: ChatHeaderProps) {
  return (
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
  );
}
