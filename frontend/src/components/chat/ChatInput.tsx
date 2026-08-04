import type { Dispatch, SetStateAction } from "react";
import type { SyntheticEvent } from "react";

interface ChatInputProps {
  input: string;
  setInput: Dispatch<SetStateAction<string>>;
  isLoading: boolean;
  onSubmit: (event: SyntheticEvent<HTMLFormElement>) => Promise<void>;
}

export function ChatInput({
  input,
  setInput,
  isLoading,
  onSubmit,
}: ChatInputProps) {
  return (
    <form className="composer" onSubmit={onSubmit}>
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
  );
}
