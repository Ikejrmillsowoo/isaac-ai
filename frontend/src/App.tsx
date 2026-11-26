import { ChatHeader } from "./components/chat/ChatHeader";
import { ChatInput } from "./components/chat/ChatInput";
import { ChatWindow } from "./components/chat/ChatWindow";
import KnowledgePanel from "./components/knowledge/KnowledgePanel";
import { ConversationSidebar } from "./components/sidebar/ConversationSidebar";
import { WorkspaceSelector } from "./components/sidebar/WorkspaceSelector";
import {
  ConversationProvider,
  useConversationContext,
} from "./context/ConversationContext";
import {
  WorkspaceProvider,
  useWorkspaceContext,
} from "./context/WorkspaceContext";
import { useChat } from "./hooks/useChat";
import "./App.css";

function ConversationArea() {
  const { selectedWorkspace } = useWorkspaceContext();

  if (!selectedWorkspace) {
    return <div className="p-4">Select a workspace.</div>;
  }

  return (
    <ConversationProvider workspaceId={selectedWorkspace.id}>
      <IsaacAiApp />
    </ConversationProvider>
  );
}

function IsaacAiApp() {
  const {
    workspaces,
    selectedWorkspace,
    selectWorkspace,
    isLoading: workspacesLoading,
    error: workspacesError,
  } = useWorkspaceContext();

  const {
    conversations,
    selectedConversation,
    selectConversation,
    createNewConversation,
    isLoading: conversationsLoading,
    isCreating,
    error: conversationsError,
  } = useConversationContext();

  const workspaceId = selectedWorkspace?.id ?? "";
  const conversationId = selectedConversation?.id ?? "";

  const { input, setInput, messages, isLoading, error, handleSubmit } = useChat(
    {
      workspaceId,
      conversationId,
    },
  );

  return (
    <main className="application-shell">
      <aside className="conversation-sidebar">
        <WorkspaceSelector
          workspaces={workspaces}
          selectedWorkspaceId={selectedWorkspace?.id}
          isLoading={workspacesLoading}
          error={workspacesError}
          onSelect={selectWorkspace}
        />

        <ConversationSidebar
          conversations={conversations}
          selectedConversationId={selectedConversation?.id}
          isLoading={conversationsLoading}
          isCreating={isCreating}
          error={conversationsError}
          onSelect={selectConversation}
          onCreate={createNewConversation}
        />

        {selectedWorkspace && (
          <KnowledgePanel workspaceId={selectedWorkspace.id} />
        )}
      </aside>

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
    <WorkspaceProvider>
      <ConversationArea />
    </WorkspaceProvider>
  );
}

export default App;
