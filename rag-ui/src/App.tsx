import { useState } from "react";
import ChatPage from "./pages/ChatPage";
import DocumentsPage from "./pages/DocumentsPage";
import SettingsPage from "./pages/SettingsPage";
import LoginPage from "./pages/LoginPage";

type Tab = "chat" | "documents" | "settings";

export default function App() {
  const [token, setToken] = useState<string | null>(
    localStorage.getItem("jwt"),
  );
  const [tab, setTab] = useState<Tab>("chat");

  if (!token) return <LoginPage onLogin={setToken} />;

  function logout() {
    localStorage.removeItem("jwt");
    setToken(null);
  }

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
          {(["chat", "documents", "settings"] as Tab[]).map((t) => (
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
              {t === "chat"
                ? "Chat"
                : t === "documents"
                  ? "Documents"
                  : "Settings"}
            </button>
          ))}
        </nav>
        <button
          onClick={logout}
          style={{
            marginLeft: "auto",
            background: "none",
            border: "none",
            color: "var(--gray-500)",
            fontSize: 13,
            cursor: "pointer",
          }}
        >
          Logout
        </button>
      </header>
      <main style={{ flex: 1, display: "flex", flexDirection: "column" }}>
        {tab === "chat" ? (
          <ChatPage />
        ) : tab === "documents" ? (
          <DocumentsPage />
        ) : (
          <SettingsPage />
        )}
      </main>
    </div>
  );
}
