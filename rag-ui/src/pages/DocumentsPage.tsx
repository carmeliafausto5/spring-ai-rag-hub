import { useState, useEffect, useRef } from "react";
import { Trash2, Upload, FileText } from "lucide-react";

interface Doc {
  id: string;
  title: string;
  sourceUri: string;
}

export default function DocumentsPage() {
  const [docs, setDocs] = useState<Doc[]>([]);
  const [file, setFile] = useState<File | null>(null);
  const [title, setTitle] = useState("");
  const [uploading, setUploading] = useState(false);
  const [notice, setNotice] = useState<{
    type: "ok" | "err";
    text: string;
  } | null>(null);
  const [dragging, setDragging] = useState(false);
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
    if (!file) return;
    setUploading(true);
    setNotice(null);
    const form = new FormData();
    form.append("file", file);
    if (title.trim()) form.append("title", title.trim());
    try {
      const res = await fetch("/api/v1/documents/upload", {
        method: "POST",
        body: form,
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setNotice({ type: "ok", text: `"${file.name}" uploaded successfully.` });
      setFile(null);
      setTitle("");
      fetchDocs();
    } catch (e) {
      setNotice({
        type: "err",
        text: "Upload failed: " + (e instanceof Error ? e.message : String(e)),
      });
    } finally {
      setUploading(false);
    }
  }

  async function deleteDoc(id: string) {
    await fetch(`/api/v1/documents/${id}`, { method: "DELETE" });
    setDocs((prev) => prev.filter((d) => d.id !== id));
  }

  function onDrop(e: React.DragEvent) {
    e.preventDefault();
    setDragging(false);
    const f = e.dataTransfer.files[0];
    if (f) setFile(f);
  }

  return (
    <div style={{ maxWidth: 860, margin: "32px auto", padding: "0 16px" }}>
      {/* Upload area */}
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
            {file ? (
              <span style={{ color: "var(--blue)", fontWeight: 500 }}>
                {file.name}
              </span>
            ) : (
              <>
                Drag & drop or{" "}
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
            accept=".pdf,.docx,.html,.md,.txt"
            style={{ display: "none" }}
            onChange={(e) => {
              const f = e.target.files?.[0];
              if (f) setFile(f);
            }}
          />
        </div>

        <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
          <input
            type="text"
            placeholder="Title (optional)"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            style={{
              flex: 1,
              border: "1px solid var(--gray-200)",
              borderRadius: "var(--radius)",
              padding: "7px 12px",
              fontFamily: "inherit",
              fontSize: 14,
              outline: "none",
            }}
          />
          <button
            onClick={upload}
            disabled={!file || uploading}
            style={{
              background: "var(--blue)",
              color: "#fff",
              border: "none",
              borderRadius: "var(--radius)",
              padding: "7px 16px",
              fontWeight: 500,
              opacity: !file || uploading ? 0.5 : 1,
              display: "flex",
              alignItems: "center",
              gap: 6,
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

      {/* Document list */}
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
                <th style={{ padding: "8px 20px", width: 48 }}></th>
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
                    <button
                      onClick={() => deleteDoc(doc.id)}
                      style={{
                        background: "none",
                        border: "none",
                        color: "var(--gray-400)",
                        padding: 4,
                        borderRadius: 4,
                        display: "flex",
                        alignItems: "center",
                      }}
                      title="Delete"
                    >
                      <Trash2 size={15} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
