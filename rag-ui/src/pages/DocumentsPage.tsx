import { useState, useEffect, useRef } from "react";
import { Trash2, Upload, FileText, Eye, X, Edit2 } from "lucide-react";

interface Doc {
  id: string;
  title: string;
  sourceUri: string;
}

export default function DocumentsPage() {
  const [docs, setDocs] = useState<Doc[]>([]);
  const [files, setFiles] = useState<File[]>([]);
  const [title, setTitle] = useState("");
  const [tags, setTags] = useState("");
  const [uploading, setUploading] = useState(false);
  const [notice, setNotice] = useState<{
    type: "ok" | "err";
    text: string;
  } | null>(null);
  const [dragging, setDragging] = useState(false);
  const [preview, setPreview] = useState<{
    id: string;
    chunks: string[];
  } | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchDocs();
  }, []);

  async function fetchDocs() {
    try {
      const res = await fetch("/api/v1/documents");
      const data = await res.json();
      setDocs(Array.isArray(data) ? data : (data.documents ?? []));
    } catch {
      setDocs([]);
    }
  }

  async function upload() {
    if (files.length === 0) return;
    setUploading(true);
    setNotice(null);
    const auth = localStorage.getItem("jwt") ?? "";
    let failed = 0;
    for (const f of files) {
      const form = new FormData();
      form.append("file", f);
      if (files.length === 1 && title.trim())
        form.append("title", title.trim());
      if (tags.trim()) form.append("tags", tags.trim());
      try {
        const res = await fetch("/api/v1/documents/upload", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${auth}`,
            "X-API-Key": import.meta.env.VITE_API_KEY ?? "",
          },
          body: form,
        });
        if (!res.ok) failed++;
      } catch {
        failed++;
      }
    }
    const total = files.length;
    if (failed === 0) {
      setNotice({
        type: "ok",
        text: `${total} file${total > 1 ? "s" : ""} uploaded successfully.`,
      });
    } else {
      setNotice({ type: "err", text: `${failed} of ${total} uploads failed.` });
    }
    setFiles([]);
    setTitle("");
    setTags("");
    setUploading(false);
    fetchDocs();
  }

  async function deleteDoc(id: string, docTitle: string) {
    if (!window.confirm(`Delete "${docTitle}"?`)) return;
    await fetch(`/api/v1/documents/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("jwt") ?? ""}`,
        "X-API-Key": import.meta.env.VITE_API_KEY ?? "",
      },
    });
    setDocs((prev) => prev.filter((d) => d.id !== id));
  }

  async function editTags(id: string) {
    const input = window.prompt("Enter tags (comma-separated):");
    if (input === null) return;
    const tagList = input
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean);
    await fetch(`/api/v1/documents/${id}/tags`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("jwt") ?? ""}`,
      },
      body: JSON.stringify(tagList),
    });
    fetchDocs();
  }

  async function previewDoc(id: string) {
    try {
      const res = await fetch(`/api/v1/documents/${id}/chunks?limit=3`);
      const data = await res.json();
      const chunks: string[] = Array.isArray(data) ? data : (data.chunks ?? []);
      setPreview({ id, chunks });
    } catch {
      setPreview({ id, chunks: [] });
    }
  }

  function onDrop(e: React.DragEvent) {
    e.preventDefault();
    setDragging(false);
    const dropped = Array.from(e.dataTransfer.files);
    if (dropped.length) setFiles(dropped);
  }

  return (
    <div style={{ maxWidth: 860, margin: "32px auto", padding: "0 16px" }}>
      <div
        style={{
          background: "#fff",
          border: "1px solid var(--gray-200)",
          borderRadius: "var(--radius)",
          padding: 24,
          marginBottom: 24,
          boxShadow: "var(--shadow)",
        }}
      >
        <h2 style={{ fontWeight: 600, fontSize: 15, marginBottom: 16 }}>
          Upload Document
        </h2>
        <div
          onClick={() => inputRef.current?.click()}
          onDragOver={(e) => {
            e.preventDefault();
            setDragging(true);
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={onDrop}
          style={{
            border: `2px dashed ${dragging ? "var(--blue)" : "var(--gray-200)"}`,
            borderRadius: "var(--radius)",
            padding: "28px 16px",
            textAlign: "center",
            cursor: "pointer",
            background: dragging ? "var(--blue-light)" : "var(--gray-50)",
            transition: "all 0.15s",
            marginBottom: 12,
          }}
        >
          <Upload
            size={20}
            style={{ color: "var(--gray-400)", marginBottom: 8 }}
          />
          <div style={{ color: "var(--gray-600)" }}>
            {files.length > 0 ? (
              <span style={{ color: "var(--blue)", fontWeight: 500 }}>
                {files.length === 1
                  ? files[0].name
                  : `${files.length} files selected`}
              </span>
            ) : (
              <>
                <span>Drag & drop or </span>
                <span style={{ color: "var(--blue)", fontWeight: 500 }}>
                  click to browse
                </span>
              </>
            )}
          </div>
          <div style={{ fontSize: 12, color: "var(--gray-400)", marginTop: 4 }}>
            PDF, DOCX, HTML, MD, TXT
          </div>
          <input
            ref={inputRef}
            type="file"
            multiple
            accept=".pdf,.docx,.html,.md,.txt"
            style={{ display: "none" }}
            onChange={(e) => {
              const f = Array.from(e.target.files ?? []);
              if (f.length) setFiles(f);
            }}
          />
        </div>
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 8,
            marginBottom: 8,
          }}
        >
          {files.length <= 1 && (
            <input
              type="text"
              placeholder="Title (optional)"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              style={{
                border: "1px solid var(--gray-200)",
                borderRadius: "var(--radius)",
                padding: "7px 12px",
                fontFamily: "inherit",
                fontSize: 14,
                outline: "none",
              }}
            />
          )}
          <input
            type="text"
            placeholder="Tags (comma-separated, optional)"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            style={{
              border: "1px solid var(--gray-200)",
              borderRadius: "var(--radius)",
              padding: "7px 12px",
              fontFamily: "inherit",
              fontSize: 14,
              outline: "none",
            }}
          />
        </div>
        <div style={{ display: "flex", justifyContent: "flex-end" }}>
          <button
            onClick={upload}
            disabled={files.length === 0 || uploading}
            style={{
              background: "var(--blue)",
              color: "#fff",
              border: "none",
              borderRadius: "var(--radius)",
              padding: "7px 16px",
              fontWeight: 500,
              opacity: files.length === 0 || uploading ? 0.5 : 1,
              display: "flex",
              alignItems: "center",
              gap: 6,
              cursor: "pointer",
            }}
          >
            <Upload size={14} />
            {uploading ? "Uploading…" : "Upload"}
          </button>
        </div>
        {notice && (
          <div
            style={{
              marginTop: 10,
              fontSize: 13,
              color: notice.type === "ok" ? "#16a34a" : "#dc2626",
              padding: "6px 10px",
              background: notice.type === "ok" ? "#f0fdf4" : "#fef2f2",
              borderRadius: 6,
            }}
          >
            {notice.text}
          </div>
        )}
      </div>

      <div
        style={{
          background: "#fff",
          border: "1px solid var(--gray-200)",
          borderRadius: "var(--radius)",
          boxShadow: "var(--shadow)",
          overflow: "hidden",
        }}
      >
        <div
          style={{
            padding: "14px 20px",
            borderBottom: "1px solid var(--gray-100)",
            fontWeight: 600,
            fontSize: 15,
          }}
        >
          Documents ({docs.length})
        </div>
        {docs.length === 0 ? (
          <div
            style={{
              padding: 40,
              textAlign: "center",
              color: "var(--gray-400)",
            }}
          >
            <FileText size={28} style={{ marginBottom: 8, opacity: 0.4 }} />
            <div>No documents yet</div>
          </div>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr
                style={{
                  background: "var(--gray-50)",
                  fontSize: 12,
                  color: "var(--gray-600)",
                  textTransform: "uppercase",
                  letterSpacing: "0.04em",
                }}
              >
                <th
                  style={{
                    padding: "8px 20px",
                    textAlign: "left",
                    fontWeight: 500,
                  }}
                >
                  Title
                </th>
                <th
                  style={{
                    padding: "8px 20px",
                    textAlign: "left",
                    fontWeight: 500,
                  }}
                >
                  Source
                </th>
                <th
                  style={{
                    padding: "8px 20px",
                    textAlign: "left",
                    fontWeight: 500,
                  }}
                >
                  ID
                </th>
                <th style={{ padding: "8px 20px", width: 80 }}></th>
              </tr>
            </thead>
            <tbody>
              {docs.map((doc, i) => (
                <tr
                  key={doc.id}
                  style={{
                    borderTop: i === 0 ? "none" : "1px solid var(--gray-100)",
                  }}
                >
                  <td style={{ padding: "10px 20px", fontWeight: 500 }}>
                    {doc.title || "—"}
                  </td>
                  <td
                    style={{
                      padding: "10px 20px",
                      color: "var(--gray-600)",
                      maxWidth: 260,
                      overflow: "hidden",
                      textOverflow: "ellipsis",
                      whiteSpace: "nowrap",
                    }}
                  >
                    {doc.sourceUri || "—"}
                  </td>
                  <td
                    style={{
                      padding: "10px 20px",
                      color: "var(--gray-400)",
                      fontFamily: "monospace",
                      fontSize: 12,
                    }}
                  >
                    {doc.id}
                  </td>
                  <td style={{ padding: "10px 20px" }}>
                    <div style={{ display: "flex", gap: 4 }}>
                      <button
                        onClick={() => previewDoc(doc.id)}
                        style={{
                          background: "none",
                          border: "none",
                          color: "var(--gray-400)",
                          padding: 4,
                          borderRadius: 4,
                          display: "flex",
                          alignItems: "center",
                          cursor: "pointer",
                        }}
                        title="Preview"
                      >
                        <Eye size={15} />
                      </button>
                      <button
                        onClick={() => editTags(doc.id)}
                        style={{
                          background: "none",
                          border: "none",
                          color: "var(--gray-400)",
                          padding: 4,
                          borderRadius: 4,
                          display: "flex",
                          alignItems: "center",
                          cursor: "pointer",
                        }}
                        title="Edit tags"
                      >
                        <Edit2 size={15} />
                      </button>
                      <button
                        onClick={() => deleteDoc(doc.id, doc.title)}
                        style={{
                          background: "none",
                          border: "none",
                          color: "var(--gray-400)",
                          padding: 4,
                          borderRadius: 4,
                          display: "flex",
                          alignItems: "center",
                          cursor: "pointer",
                        }}
                        title="Delete"
                      >
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {preview && (
          <div style={{ borderTop: "1px solid var(--gray-200)", padding: 20 }}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: 12,
              }}
            >
              <span style={{ fontWeight: 600, fontSize: 14 }}>
                Preview — {preview.id}
              </span>
              <button
                onClick={() => setPreview(null)}
                style={{
                  background: "none",
                  border: "none",
                  color: "var(--gray-400)",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                }}
              >
                <X size={16} />
              </button>
            </div>
            {preview.chunks.length === 0 ? (
              <div style={{ color: "var(--gray-400)", fontSize: 13 }}>
                No chunks available.
              </div>
            ) : (
              preview.chunks.map((chunk, i) => (
                <div
                  key={i}
                  style={{
                    background: "var(--gray-50)",
                    border: "1px solid var(--gray-200)",
                    borderRadius: "var(--radius)",
                    padding: "10px 14px",
                    marginBottom: 8,
                    fontSize: 13,
                    color: "var(--gray-600)",
                    lineHeight: 1.6,
                  }}
                >
                  {typeof chunk === "string"
                    ? chunk.slice(0, 200)
                    : JSON.stringify(chunk).slice(0, 200)}
                  {(typeof chunk === "string" ? chunk : JSON.stringify(chunk))
                    .length > 200 && "…"}
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}
