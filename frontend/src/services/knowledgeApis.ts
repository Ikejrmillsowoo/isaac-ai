export interface KnowledgeDocument {
  id: string;
  name: string;
  originalFilename: string;
  contentType: string;
  status: "UPLOADED" | "PROCESSING" | "READY" | "FAILED";
  createdAt: string;
  updatedAt: string;
}

const API_BASE_URL = "http://localhost:8080/api";

export async function fetchKnowledgeDocuments(
  workspaceId: string,
): Promise<KnowledgeDocument[]> {
  const response = await fetch(
    `${API_BASE_URL}/workspaces/${workspaceId}/knowledge`,
  );

  if (!response.ok) {
    throw new Error(`Failed to load knowledge documents: ${response.status}`);
  }

  return response.json();
}

export async function uploadKnowledgeDocument(
  workspaceId: string,
  file: File,
): Promise<KnowledgeDocument> {
  const formData = new FormData();

  formData.append("file", file);

  const response = await fetch(
    `${API_BASE_URL}/workspaces/${workspaceId}/knowledge/upload`,
    {
      method: "POST",
      body: formData,
    },
  );

  if (!response.ok) {
    throw new Error(`Failed to upload document: ${response.status}`);
  }

  return response.json();
}

export async function deleteKnowledgeDocument(
  workspaceId: string,
  documentId: string,
): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/workspaces/${workspaceId}/knowledge/${documentId}`,
    {
      method: "DELETE",
    },
  );

  if (!response.ok) {
    throw new Error(`Failed to delete document: ${response.status}`);
  }
}
