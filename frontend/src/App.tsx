import { ChatHeader } from "./components/chat/ChatHeader";
import { ChatInput } from "./components/chat/ChatInput";
import { ChatWindow } from "./components/chat/ChatWindow";
import { ConversationSidebar } from "./components/sidebar/ConversationSidebar";
import {
  ConversationProvider,
  useConversationContext,
} from "./context/ConversationContext";
import { useChat } from "./hooks/useChat";
import "./App.css";

const WORKSPACE_ID = "4aa0a5a1-be58-48a3-977a-97a2e5b74bf6";

function IsaacAiApp() {
  const {
    conversations,
    selectedConversation,
    selectConversation,
    createNewConversation,
    isLoading: conversationsLoading,
    isCreating,
    error: conversationsError,
  } = useConversationContext();

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
        isCreating={isCreating}
        error={conversationsError}
        onSelect={selectConversation}
        onCreate={createNewConversation}
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

function App() {
  return (
    <ConversationProvider workspaceId={WORKSPACE_ID}>
      <IsaacAiApp />
    </ConversationProvider>
  );
}

export default App;
