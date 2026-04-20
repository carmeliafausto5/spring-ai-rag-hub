import { useState, useRef, useEffect } from "react";
import { Send } from "lucide-react";

interface SourceRef {
  documentId: string;
  title: string;
  excerpt: string;
  score: number;
}

interface Message {
  id: string;
  role: "user" | "assistant";
  content: string;
  sources?: SourceRef[];
  meta?: { provider?: string; latencyMs?: number };
  done?: boolean;
}

interface Conversation {
  id: string;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
}

export default function ChatPage() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [streaming, setStreaming] = useState(false);
  const [sessionId, setSessionId] = useState<string>(() => crypto.randomUUID());
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [hoveredId, setHoveredId] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);
  const abortRef = useRef<AbortController | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  useEffect(
    () => () => {
      abortRef.current?.abort();
    },
    [],
  );

  useEffect(() => {
    fetchConversations();
  }, []);

  async function fetchConversations() {
    try {
      const res = await fetch("/api/v1/conversations", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwt") ?? ""}`,
        },
      });
      if (res.ok) setConversations(await res.json());
    } catch {}
  }

  async function loadConversation(id: string) {
    try {
      const res = await fetch(`/api/v1/conversations/${id}/messages`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwt") ?? ""}`,
        },
      });
      if (!res.ok) return;
      const data: { role: "user" | "assistant"; content: string }[] =
        await res.json();
      setMessages(
        data.map((m) => ({
          id: crypto.randomUUID(),
          role: m.role,
          content: m.content,
          done: true,
        })),
      );
      setSessionId(id);
    } catch {}
  }

  function newChat() {
    setSessionId(crypto.randomUUID());
    setMessages([]);
  }

  async function send() {
    const question = input.trim();
    if (!question || streaming) return;
    setInput("");
    const history = messages.map((m) => ({ role: m.role, content: m.content }));
    const userMsg: Message = {
      id: crypto.randomUUID(),
      role: "user",
      content: question,
    };
    setMessages((prev) => [
      ...prev,
      userMsg,
      { id: crypto.randomUUID(), role: "assistant", content: "", done: false },
    ]);
    setStreaming(true);
    abortRef.current = new AbortController();
    try {
      const res = await fetch("/api/v1/rag/query/stream", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("jwt") ?? ""}`,
        },
        body: JSON.stringify({ question, history, sessionId }),
        signal: abortRef.current.signal,
      });
      if (!res.body) throw new Error("No response body");
      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      let buf = "";
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buf += decoder.decode(value, { stream: true });
        const blocks = buf.split("\n\n");
        buf = blocks.pop() ?? "";
        for (const block of blocks) {
          const lines = block.split("\n");
          const eventLine = lines.find((l) => l.startsWith("event:"));
          const dataLine = lines.find((l) => l.startsWith("data:"));
          if (!eventLine || !dataLine) continue;
          const event = eventLine.slice(6).trim();
          const data = dataLine.slice(5).trim();
          if (event === "token") {
            setMessages((prev) => {
              const next = [...prev];
              const last = next[next.length - 1];
              if (last.role === "assistant") last.content += data;
              return next;
            });
          } else if (event === "done") {
            try {
              const parsed = JSON.parse(data);
              setMessages((prev) => {
                const next = [...prev];
                const last = next[next.length - 1];
                if (last.role === "assistant") {
                  last.done = true;
                  last.sources = parsed.sources ?? [];
                  last.meta = {
                    provider: parsed.provider,
                    latencyMs: parsed.latencyMs ?? parsed.latency,
                  };
                }
                return next;
              });
            } catch {
              setMessages((prev) => {
                const next = [...prev];
                const last = next[next.length - 1];
                if (last.role === "assistant") last.done = true;
                return next;
              });
            }
          }
        }
      }
    } catch (e) {
      if (e instanceof Error && e.name === "AbortError") return;
      setMessages((prev) => {
        const next = [...prev];
        const last = next[next.length - 1];
        if (last.role === "assistant") {
          last.content =
            "Error: " + (e instanceof Error ? e.message : String(e));
          last.done = true;
        }
        return next;
      });
    } finally {
      setStreaming(false);
      fetchConversations();
    }
  }

  return (
    <div
      style={{
        flex: 1,
        display: "flex",
        flexDirection: "row",
        height: "calc(100vh - 52px)",
      }}
    >
      <div
        style={{
          width: 240,
          borderRight: "1px solid var(--gray-200)",
          background: "var(--gray-50)",
          display: "flex",
          flexDirection: "column",
          overflowY: "auto",
          flexShrink: 0,
        }}
      >
        <div style={{ padding: "12px 12px 8px" }}>
          <button
            onClick={newChat}
            style={{
              width: "100%",
              background: "var(--blue)",
              color: "#fff",
              border: "none",
              borderRadius: "var(--radius)",
              padding: "8px 12px",
              fontSize: 13,
              cursor: "pointer",
            }}
          >
            New Chat
          </button>
        </div>
        <div style={{ flex: 1, overflowY: "auto" }}>
          {conversations.map((c) => {
            const active = c.id === sessionId;
            const hovered = c.id === hoveredId;
            return (
              <div
                key={c.id}
                onClick={() => loadConversation(c.id)}
                onMouseEnter={() => setHoveredId(c.id)}
                onMouseLeave={() => setHoveredId(null)}
                style={{
                  padding: "8px 12px",
                  cursor: "pointer",
                  background: active
                    ? "var(--blue-light)"
                    : hovered
                      ? "var(--gray-100)"
                      : "transparent",
                  borderLeft: active
                    ? "2px solid var(--blue)"
                    : "2px solid transparent",
                }}
              >
                <div
                  style={{
                    fontSize: 13,
                    fontWeight: active ? 600 : 400,
                    color: "var(--gray-800)",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                  }}
                >
                  {c.id.slice(0, 8)}
                </div>
                <div
                  style={{
                    fontSize: 11,
                    color: "var(--gray-400)",
                    marginTop: 2,
                  }}
                >
                  {c.messageCount} messages
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <div
        style={{
          flex: 1,
          display: "flex",
          flexDirection: "column",
          minWidth: 0,
        }}
      >
        <div style={{ flex: 1, overflowY: "auto", padding: "24px 0" }}>
          <div
            style={{
              maxWidth: 720,
              margin: "0 auto",
              padding: "0 16px",
              display: "flex",
              flexDirection: "column",
              gap: 16,
            }}
          >
            {messages.length === 0 && (
              <div
                style={{
                  textAlign: "center",
                  color: "var(--gray-400)",
                  marginTop: 80,
                }}
              >
                Ask a question about your documents
              </div>
            )}
            {messages.map((m) => (
              <div
                key={m.id}
                style={{
                  display: "flex",
                  justifyContent: m.role === "user" ? "flex-end" : "flex-start",
                }}
              >
                <div style={{ maxWidth: "75%" }}>
                  <div
                    style={{
                      padding: "10px 14px",
                      borderRadius: "var(--radius)",
                      background: m.role === "user" ? "var(--blue)" : "#fff",
                      color: m.role === "user" ? "#fff" : "var(--gray-900)",
                      border:
                        m.role === "assistant"
                          ? "1px solid var(--gray-200)"
                          : "none",
                      boxShadow: "var(--shadow)",
                      whiteSpace: "pre-wrap",
                      lineHeight: 1.6,
                    }}
                  >
                    {m.content}
                    {m.role === "assistant" && !m.done && (
                      <span style={{ opacity: 0.4 }}>▋</span>
                    )}
                  </div>
                  {m.role === "assistant" && m.done && m.meta && (
                    <div
                      style={{
                        fontSize: 11,
                        color: "var(--gray-400)",
                        marginTop: 4,
                        paddingLeft: 2,
                      }}
                    >
                      {[
                        m.meta.provider,
                        m.meta.latencyMs != null
                          ? `${m.meta.latencyMs}ms`
                          : null,
                      ]
                        .filter(Boolean)
                        .join(" · ")}
                    </div>
                  )}
                  {m.role === "assistant" &&
                    m.done &&
                    m.sources &&
                    m.sources.length > 0 && (
                      <div
                        style={{
                          marginTop: 8,
                          display: "flex",
                          flexDirection: "column",
                          gap: 6,
                        }}
                      >
                        {m.sources.map((s, si) => (
                          <div
                            key={si}
                            style={{
                              background: "var(--gray-50)",
                              border: "1px solid var(--gray-200)",
                              borderRadius: "var(--radius)",
                              padding: "6px 10px",
                              fontSize: 12,
                            }}
                          >
                            <div
                              style={{
                                fontWeight: 600,
                                color: "var(--gray-700)",
                                marginBottom: 2,
                              }}
                            >
                              [{si + 1}] {s.title || s.documentId}
                            </div>
                            {s.excerpt && (
                              <div
                                style={{
                                  color: "var(--gray-500)",
                                  lineHeight: 1.4,
                                }}
                              >
                                {s.excerpt}
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                </div>
              </div>
            ))}
            <div ref={bottomRef} />
          </div>
        </div>

        <div
          style={{
            borderTop: "1px solid var(--gray-200)",
            background: "#fff",
            padding: "12px 16px",
          }}
        >
          <div
            style={{
              maxWidth: 720,
              margin: "0 auto",
              display: "flex",
              gap: 8,
              alignItems: "flex-end",
            }}
          >
            <textarea
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  send();
                }
              }}
              placeholder="Ask a question… (Enter to send, Shift+Enter for newline)"
              rows={1}
              style={{
                flex: 1,
                resize: "none",
                border: "1px solid var(--gray-200)",
                borderRadius: "var(--radius)",
                padding: "8px 12px",
                fontFamily: "inherit",
                fontSize: 14,
                outline: "none",
                lineHeight: 1.5,
                maxHeight: 120,
                overflowY: "auto",
              }}
            />
            <button
              onClick={send}
              disabled={streaming || !input.trim()}
              style={{
                background: "var(--blue)",
                color: "#fff",
                border: "none",
                borderRadius: "var(--radius)",
                padding: "8px 14px",
                display: "flex",
                alignItems: "center",
                gap: 6,
                opacity: streaming || !input.trim() ? 0.5 : 1,
                cursor: streaming || !input.trim() ? "not-allowed" : "pointer",
              }}
            >
              <Send size={15} />
              Send
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
