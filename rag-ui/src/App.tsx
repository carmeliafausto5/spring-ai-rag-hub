import { useState } from "react";
import ChatPage from "./pages/ChatPage";
import DocumentsPage from "./pages/DocumentsPage";

type Tab = "chat" | "documents";

export default function App() {
  const [tab, setTab] = useState<Tab>("chat");

  return (
    <div
      style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}
    >
      <header
        style={{
          background: "#fff",
          borderBottom: "1px solid var(--gray-200)",
          padding: "0 24px",
          display: "flex",
          alignItems: "center",
          gap: "32px",
          height: "52px",
          boxShadow: "var(--shadow)",
        }}
      >
        <span
          style={{
            fontWeight: 600,
            fontSize: 15,
            color: "var(--gray-900)",
            letterSpacing: "-0.01em",
          }}
        >
          RAG Hub
        </span>
        <nav style={{ display: "flex", gap: "4px", height: "100%" }}>
          {(["chat", "documents"] as Tab[]).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              style={{
                background: "none",
                border: "none",
                padding: "0 12px",
                height: "100%",
                fontWeight: tab === t ? 600 : 400,
                color: tab === t ? "var(--blue)" : "var(--gray-600)",
                borderBottom:
                  tab === t ? "2px solid var(--blue)" : "2px solid transparent",
                textTransform: "capitalize",
                transition: "color 0.15s",
              }}
            >
              {t === "chat" ? "Chat" : "Documents"}
            </button>
          ))}
        </nav>
      </header>
      <main style={{ flex: 1, display: "flex", flexDirection: "column" }}>
        {tab === "chat" ? <ChatPage /> : <DocumentsPage />}
      </main>
    </div>
  );
}
