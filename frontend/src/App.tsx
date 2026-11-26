import { useState } from "react";
import { ChatHeader } from "./components/chat/ChatHeader";
import { ChatInput } from "./components/chat/ChatInput";
import { ChatWindow } from "./components/chat/ChatWindow";
import KnowledgePanel from "./components/knowledge/KnowledgePanel";
import MemoryPanel from "./components/memory/MemoryPanel";
import { ConversationSidebar } from "./components/sidebar/ConversationSidebar";
import { WorkspaceSelector } from "./components/sidebar/WorkspaceSelector";
import { ConversationProvider } from "./context/ConversationContext";
import { useConversationContext } from "./context/useConversationContext";
import { WorkspaceProvider } from "./context/WorkspaceContext";
import { useWorkspaceContext } from "./context/useWorkspaceContext";
import { useChat } from "./hooks/useChat";
import "./App.css";

function ConversationArea() {
  const { selectedWorkspace, isLoading, error, refreshWorkspaces } = useWorkspaceContext();

  if (!selectedWorkspace) {
    return <main className="application-shell"><aside className="conversation-sidebar">
      <h2>Welcome to Isaac AI</h2>
      {isLoading ? <p>Loading workspaces…</p> : <WorkspaceCreation />}
      {error && <><p role="alert">{error}</p><button onClick={() => void refreshWorkspaces()}>Retry</button></>}
    </aside></main>;
  }

  return (
    <ConversationProvider key={selectedWorkspace.id} workspaceId={selectedWorkspace.id}>
      <IsaacAiApp />
    </ConversationProvider>
  );
}

function WorkspaceCreation() {
  const { createNewWorkspace, isCreating } = useWorkspaceContext();
  const [name, setName] = useState("");
  return <details open>
    <summary>Create workspace</summary>
    <form onSubmit={(event) => {
      event.preventDefault();
      if (!name.trim() || isCreating) return;
      void createNewWorkspace({
        name: name.trim(),
        description: "",
        color: "",
        systemPrompt: "You are Isaac AI, a private personal assistant. Be clear, practical, and accurate. Do not invent facts.",
      });
    }}>
      <label>Workspace name <input required maxLength={100} value={name} onChange={event => setName(event.target.value)} /></label>
      <button disabled={isCreating || !name.trim()} type="submit">{isCreating ? "Creating…" : "Create workspace"}</button>
    </form>
  </details>;
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

        <WorkspaceCreation />

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
          <>
            <KnowledgePanel workspaceId={selectedWorkspace.id} />
            <MemoryPanel key={selectedWorkspace.id} workspaceId={selectedWorkspace.id} chatBusy={isLoading} />
          </>
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
