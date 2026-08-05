import type { Conversation } from "../../types/conversation";

interface ConversationSidebarProps {
  conversations: Conversation[];
  selectedConversationId?: string;
  isLoading: boolean;
  isCreating: boolean;
  error: string;
  onSelect: (conversation: Conversation) => void;
  onCreate: () => Promise<void>;
}

export function ConversationSidebar({
  conversations,
  selectedConversationId,
  isLoading,
  isCreating,
  error,
  onSelect,
  onCreate,
}: ConversationSidebarProps) {
  return (
    <aside className="conversation-sidebar">
      <div className="sidebar-header">
        <div>
          <p className="eyebrow">Conversations</p>
          <h2>History</h2>
        </div>
        <button
          type="button"
          className="new-conversation-button"
          onClick={() => void onCreate()}
          disabled={isCreating}
        >
          {isCreating ? "Creating…" : "+ New"}
        </button>
      </div>

      <div className="conversation-list">
        {isLoading && <p className="sidebar-message">Loading conversations…</p>}

        {!isLoading && error && (
          <p className="sidebar-error" role="alert">
            {error}
          </p>
        )}

        {!isLoading && !error && conversations.length === 0 && (
          <p className="sidebar-message">No conversations yet.</p>
        )}

        {!isLoading &&
          !error &&
          conversations.map((conversation) => {
            const isSelected = conversation.id === selectedConversationId;

            return (
              <button
                key={conversation.id}
                type="button"
                className={`conversation-item ${isSelected ? "selected" : ""}`}
                onClick={() => onSelect(conversation)}
              >
                <span className="conversation-title">{conversation.title}</span>

                {conversation.pinned && (
                  <span className="conversation-pin" aria-label="Pinned">
                    ●
                  </span>
                )}
              </button>
            );
          })}
      </div>
    </aside>
  );
}
