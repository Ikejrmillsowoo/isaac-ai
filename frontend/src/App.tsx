import { ChatHeader } from "./components/chat/ChatHeader";
import { ChatInput } from "./components/chat/ChatInput";
import { ChatWindow } from "./components/chat/ChatWindow";
import { useChat } from "./hooks/useChat";
import { ConversationSidebar } from "./components/sidebar/ConversationSidebar";
import { useConversations } from "./hooks/useConversations";
import "./App.css";

const WORKSPACE_ID = "4aa0a5a1-be58-48a3-977a-97a2e5b74bf6";

const CONVERSATION_ID = "e889b7a2-94a2-41fa-a72e-88b629861e71";

function App() {
  const {
    conversations,
    selectedConversation,
    selectConversation,
    isLoading: conversationsLoading,
    error: conversationsError,
  } = useConversations(WORKSPACE_ID, CONVERSATION_ID);

  const conversationId = selectedConversation?.id ?? "";

  const { input, setInput, messages, isLoading, error, handleSubmit } = useChat(
    {
      workspaceId: WORKSPACE_ID,
      conversationId,
    },
  );

  return (
    <main className="application-shell">
      <ConversationSidebar
        conversations={conversations}
        selectedConversationId={selectedConversation?.id}
        isLoading={conversationsLoading}
        error={conversationsError}
        onSelect={selectConversation}
      />

      <section className="app">
        <ChatHeader isLoading={isLoading} />

        <ChatWindow messages={messages} isLoading={isLoading} />

        {error && (
          <div className="error-message" role="alert">
            {error}
          </div>
        )}

        <ChatInput
          input={input}
          setInput={setInput}
          isLoading={isLoading}
          onSubmit={handleSubmit}
          disabled={!selectedConversation}
        />
      </section>
    </main>
  );
}

export default App;
