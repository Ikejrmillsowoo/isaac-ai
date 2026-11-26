import { useEffect, useRef, useState } from "react";
import {
  deleteKnowledgeDocument,
  fetchKnowledgeDocuments,
  uploadKnowledgeDocument,
} from "../../services/knowledgeApis";

import type { KnowledgeDocument } from "../../services/knowledgeApis";

interface KnowledgePanelProps {
  workspaceId: string;
}

export default function KnowledgePanel({ workspaceId }: KnowledgePanelProps) {
  const [documents, setDocuments] = useState<KnowledgeDocument[]>([]);

  const [loading, setLoading] = useState(true);

  const [uploading, setUploading] = useState(false);

  const [error, setError] = useState<string | null>(null);

  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  async function loadDocuments() {
    try {
      setLoading(true);
      setError(null);

      const data = await fetchKnowledgeDocuments(workspaceId);

      setDocuments(data);
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to load knowledge documents.",
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let active = true;
    void fetchKnowledgeDocuments(workspaceId).then(items => {
      if (active) setDocuments(items);
    }).catch(error => {
      if (active) setError(error instanceof Error ? error.message : "Failed to load knowledge documents.");
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [workspaceId]);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    try {
      setUploading(true);
      setError(null);

      await uploadKnowledgeDocument(workspaceId, file);

      await loadDocuments();
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Failed to upload document.",
      );
    } finally {
      setUploading(false);

      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  }

  async function handleDelete(documentId: string) {
    const confirmed = window.confirm(
      "Delete this document from this workspace?",
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingId(documentId);
      setError(null);

      await deleteKnowledgeDocument(workspaceId, documentId);

      setDocuments((currentDocuments) =>
        currentDocuments.filter((document) => document.id !== documentId),
      );
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Failed to delete document.",
      );
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div className="p-4">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-lg font-semibold">Knowledge</h2>

        <button
          type="button"
          disabled={uploading || loading}
          onClick={() => fileInputRef.current?.click()}
          className="rounded border px-2 py-1 text-sm"
        >
          {uploading ? "Uploading..." : "+ Add"}
        </button>

        <input
          ref={fileInputRef}
          type="file"
          accept=".pdf,.txt"
          onChange={handleFileChange}
          className="hidden"
        />
      </div>

      {error && <p className="mb-3 text-sm text-red-500">{error}</p>}

      {loading ? (
        <p className="text-sm text-gray-500">Loading knowledge...</p>
      ) : documents.length === 0 ? (
        <p className="text-sm text-gray-500">No documents in this workspace.</p>
      ) : (
        <div className="space-y-2">
          {documents.map((document) => (
            <div key={document.id} className="rounded-lg border p-3">
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <div className="font-medium">{document.name}</div>

                  <div className="mt-1 text-xs text-gray-500">
                    {document.originalFilename}
                  </div>

                  <div className="mt-1 text-xs text-gray-500">
                    Status: {document.status}
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => handleDelete(document.id)}
                  disabled={deletingId === document.id}
                  className="text-xs text-red-500 hover:text-red-700 disabled:opacity-50"
                >
                  {deletingId === document.id ? "Deleting..." : "Delete"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
