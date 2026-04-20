import { useState, useEffect } from "react";
import { Save, RefreshCw } from "lucide-react";

interface Settings {
  "rag.provider": string;
  "openai.api-key": string;
  "openai.base-url": string;
  "openai.model": string;
  "anthropic.api-key": string;
  "anthropic.base-url": string;
  "anthropic.model": string;
  "ollama.base-url": string;
  "ollama.model": string;
  "rag.rate-limit": string;
  "rag.chunk-size": string;
  "rag.chunk-overlap": string;
  [key: string]: string;
}

type Field = { key: string; label: string; type: string; options?: string[] };
type Group = { label: string; fields: Field[] };

const GROUPS: Group[] = [
  {
    label: "Provider",
    fields: [
      {
        key: "rag.provider",
        label: "Active Provider",
        type: "select",
        options: ["openai", "anthropic", "ollama"],
      },
    ],
  },
  {
    label: "OpenAI",
    fields: [
      { key: "openai.api-key", label: "API Key", type: "password" },
      { key: "openai.base-url", label: "Base URL", type: "text" },
      { key: "openai.model", label: "Model", type: "text" },
    ],
  },
  {
    label: "Anthropic",
    fields: [
      { key: "anthropic.api-key", label: "API Key", type: "password" },
      { key: "anthropic.base-url", label: "Base URL", type: "text" },
      { key: "anthropic.model", label: "Model", type: "text" },
    ],
  },
  {
    label: "Ollama",
    fields: [
      { key: "ollama.base-url", label: "Base URL", type: "text" },
      { key: "ollama.model", label: "Model", type: "text" },
    ],
  },
  {
    label: "RAG Tuning",
    fields: [
      { key: "rag.rate-limit", label: "Rate Limit (req/min)", type: "number" },
      { key: "rag.chunk-size", label: "Chunk Size (tokens)", type: "number" },
      {
        key: "rag.chunk-overlap",
        label: "Chunk Overlap (tokens)",
        type: "number",
      },
    ],
  },
];

const DEFAULTS: Settings = {
  "rag.provider": "openai",
  "openai.api-key": "",
  "openai.base-url": "https://api.openai.com",
  "openai.model": "gpt-4o",
  "anthropic.api-key": "",
  "anthropic.base-url": "https://api.anthropic.com",
  "anthropic.model": "claude-sonnet-4-6",
  "ollama.base-url": "http://localhost:11434",
  "ollama.model": "llama3.2",
  "rag.rate-limit": "20",
  "rag.chunk-size": "512",
  "rag.chunk-overlap": "64",
};

export default function SettingsPage() {
  const [settings, setSettings] = useState<Settings>({ ...DEFAULTS });
  const [dirty, setDirty] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState<{
    type: "ok" | "err";
    text: string;
  } | null>(null);

  useEffect(() => {
    fetch("/api/v1/settings")
      .then((r) => r.json())
      .then((data) => setSettings((prev) => ({ ...prev, ...data })))
      .catch(() =>
        setNotice({
          type: "err",
          text: "Failed to load settings from server.",
        }),
      );
  }, []);

  function onChange(key: string, value: string) {
    setSettings((prev) => ({ ...prev, [key]: value }));
    setDirty((prev) => ({ ...prev, [key]: value }));
  }

  async function save() {
    if (Object.keys(dirty).length === 0) return;
    setSaving(true);
    setNotice(null);
    try {
      const res = await fetch("/api/v1/settings", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("jwt") ?? ""}`,
        },
        body: JSON.stringify(dirty),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setNotice({
        type: "ok",
        text: "Settings saved. Restart the server for provider/model changes to take effect.",
      });
      setDirty({});
    } catch (e) {
      setNotice({
        type: "err",
        text: "Save failed: " + (e instanceof Error ? e.message : String(e)),
      });
    } finally {
      setSaving(false);
    }
  }

  const inputStyle = {
    width: "100%",
    border: "1px solid var(--gray-200)",
    borderRadius: "var(--radius)",
    padding: "7px 12px",
    fontFamily: "inherit",
    fontSize: 14,
    outline: "none",
    boxSizing: "border-box" as const,
  };

  return (
    <div style={{ maxWidth: 680, margin: "32px auto", padding: "0 16px" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 24,
        }}
      >
        <h1 style={{ fontWeight: 600, fontSize: 16, margin: 0 }}>Settings</h1>
        <button
          onClick={save}
          disabled={saving || Object.keys(dirty).length === 0}
          style={{
            background: "var(--blue)",
            color: "#fff",
            border: "none",
            borderRadius: "var(--radius)",
            padding: "7px 16px",
            fontWeight: 500,
            display: "flex",
            alignItems: "center",
            gap: 6,
            opacity: saving || Object.keys(dirty).length === 0 ? 0.5 : 1,
          }}
        >
          {saving ? (
            <RefreshCw
              size={14}
              style={{ animation: "spin 1s linear infinite" }}
            />
          ) : (
            <Save size={14} />
          )}
          Save
        </button>
      </div>

      {notice && (
        <div
          style={{
            marginBottom: 16,
            fontSize: 13,
            color: notice.type === "ok" ? "#16a34a" : "#dc2626",
            padding: "8px 12px",
            background: notice.type === "ok" ? "#f0fdf4" : "#fef2f2",
            borderRadius: 6,
          }}
        >
          {notice.text}
        </div>
      )}

      {GROUPS.map((group) => (
        <div
          key={group.label}
          style={{
            background: "#fff",
            border: "1px solid var(--gray-200)",
            borderRadius: "var(--radius)",
            marginBottom: 16,
            boxShadow: "var(--shadow)",
            overflow: "hidden",
          }}
        >
          <div
            style={{
              padding: "10px 20px",
              borderBottom: "1px solid var(--gray-100)",
              fontWeight: 600,
              fontSize: 13,
              color: "var(--gray-600)",
              textTransform: "uppercase",
              letterSpacing: "0.04em",
            }}
          >
            {group.label}
          </div>
          <div
            style={{
              padding: "16px 20px",
              display: "flex",
              flexDirection: "column",
              gap: 12,
            }}
          >
            {group.fields.map((field) => (
              <div
                key={field.key}
                style={{ display: "flex", alignItems: "center", gap: 12 }}
              >
                <label
                  style={{
                    width: 200,
                    fontSize: 14,
                    color: "var(--gray-700)",
                    flexShrink: 0,
                  }}
                >
                  {field.label}
                  {dirty[field.key] !== undefined && (
                    <span
                      style={{
                        marginLeft: 6,
                        width: 6,
                        height: 6,
                        borderRadius: "50%",
                        background: "var(--blue)",
                        display: "inline-block",
                      }}
                    />
                  )}
                </label>
                {field.type === "select" ? (
                  <select
                    value={settings[field.key] ?? ""}
                    onChange={(e) => onChange(field.key, e.target.value)}
                    style={{ ...inputStyle, background: "#fff" }}
                  >
                    {field.options!.map((o) => (
                      <option key={o} value={o}>
                        {o}
                      </option>
                    ))}
                  </select>
                ) : (
                  <input
                    type={field.type}
                    value={settings[field.key] ?? ""}
                    onChange={(e) => onChange(field.key, e.target.value)}
                    placeholder={
                      field.type === "password" ? "Enter to update…" : ""
                    }
                    style={inputStyle}
                  />
                )}
              </div>
            ))}
          </div>
        </div>
      ))}

      <p
        style={{ fontSize: 12, color: "var(--gray-400)", textAlign: "center" }}
      >
        API keys are masked on load. Enter a new value to update. Provider/model
        changes require server restart.
      </p>
    </div>
  );
}
