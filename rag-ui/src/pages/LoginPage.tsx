import { useState } from "react";

export default function LoginPage({
  onLogin,
}: {
  onLogin: (token: string) => void;
}) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(mode: "login" | "register") {
    setError(null);
    setLoading(true);
    try {
      const res = await fetch(`/api/v1/auth/${mode}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const data = await res.json();
      if (!res.ok) {
        setError(data.error ?? "Failed");
        return;
      }
      localStorage.setItem("jwt", data.token);
      onLogin(data.token);
    } catch {
      setError("Network error");
    } finally {
      setLoading(false);
    }
  }

  const inputStyle = {
    width: "100%",
    border: "1px solid var(--gray-200)",
    borderRadius: "var(--radius)",
    padding: "8px 12px",
    fontSize: 14,
    fontFamily: "inherit",
    outline: "none",
    boxSizing: "border-box" as const,
  };

  return (
    <div style={{ maxWidth: 360, margin: "80px auto", padding: "0 16px" }}>
      <div
        style={{
          background: "#fff",
          border: "1px solid var(--gray-200)",
          borderRadius: "var(--radius)",
          padding: 32,
          boxShadow: "var(--shadow)",
        }}
      >
        <h1
          style={{
            fontWeight: 600,
            fontSize: 18,
            marginBottom: 24,
            margin: "0 0 24px",
          }}
        >
          RAG Hub
        </h1>
        {error && (
          <div
            style={{
              color: "#dc2626",
              background: "#fef2f2",
              padding: "8px 12px",
              borderRadius: 6,
              marginBottom: 12,
              fontSize: 13,
            }}
          >
            {error}
          </div>
        )}
        <input
          style={{ ...inputStyle, marginBottom: 8 }}
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && submit("login")}
        />
        <input
          style={{ ...inputStyle, marginBottom: 20 }}
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && submit("login")}
        />
        <div style={{ display: "flex", gap: 8 }}>
          <button
            onClick={() => submit("login")}
            disabled={loading}
            style={{
              flex: 1,
              background: "var(--blue)",
              color: "#fff",
              border: "none",
              borderRadius: "var(--radius)",
              padding: "8px 0",
              fontWeight: 500,
              opacity: loading ? 0.6 : 1,
            }}
          >
            Login
          </button>
          <button
            onClick={() => submit("register")}
            disabled={loading}
            style={{
              flex: 1,
              background: "var(--gray-50)",
              border: "1px solid var(--gray-200)",
              borderRadius: "var(--radius)",
              padding: "8px 0",
              fontWeight: 500,
              opacity: loading ? 0.6 : 1,
            }}
          >
            Register
          </button>
        </div>
      </div>
    </div>
  );
}
