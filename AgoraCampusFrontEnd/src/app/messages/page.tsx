"use client";

import Image from "next/image";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { AppShell } from "../components/AppShell";
import { AppUserSummary, formatRelativeTime, MessageResponse } from "../lib/api-types";
import { useAuth } from "../lib/auth";

export default function Messages() {
  const { appUser, apiFetch } = useAuth();
  const [users, setUsers] = useState<AppUserSummary[]>([]);
  const [activeUserId, setActiveUserId] = useState<number | null>(null);
  const [draft, setDraft] = useState("");
  const [query, setQuery] = useState("");
  const [messages, setMessages] = useState<MessageResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadUsers = useCallback(async () => {
    if (!appUser) return;

    setError("");
    const response = await apiFetch("/api/users");
    if (!response.ok) {
      setError(`Could not load users (${response.status}).`);
      return;
    }

    const nextUsers = ((await response.json()) as AppUserSummary[]).filter((user) => user.id !== appUser.id);
    setUsers(nextUsers);

    const requestedUserId = Number(new URLSearchParams(window.location.search).get("userId"));
    if (requestedUserId && nextUsers.some((user) => user.id === requestedUserId)) {
      setActiveUserId(requestedUserId);
    } else if (!activeUserId && nextUsers.length > 0) {
      setActiveUserId(nextUsers[0].id);
    }
  }, [activeUserId, apiFetch, appUser]);

  const loadConversation = useCallback(async () => {
    if (!appUser || !activeUserId) return;

    setLoading(true);
    setError("");

    try {
      const response = await apiFetch(
        `/api/messages/between?userIdA=${appUser.id}&userIdB=${activeUserId}&actingUserId=${appUser.id}`,
      );
      if (!response.ok) throw new Error(`Could not load conversation (${response.status}).`);
      setMessages((await response.json()) as MessageResponse[]);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load conversation.");
    } finally {
      setLoading(false);
    }
  }, [activeUserId, apiFetch, appUser]);

  useEffect(() => {
    queueMicrotask(() => void loadUsers());
  }, [loadUsers]);

  useEffect(() => {
    queueMicrotask(() => void loadConversation());
  }, [loadConversation]);

  const normalizedQuery = query.trim().toLowerCase();
  const visibleUsers = useMemo(() => {
    if (!normalizedQuery) return users;
    return users.filter((user) =>
      [user.displayName, user.username, user.email, user.accountType].join(" ").toLowerCase().includes(normalizedQuery),
    );
  }, [normalizedQuery, users]);

  const activeUser = users.find((user) => user.id === activeUserId) ?? null;

  const handleSend = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!appUser || !activeUserId) return;

    const content = draft.trim();
    if (!content) return;

    const response = await apiFetch(`/api/messages?actingUserId=${appUser.id}`, {
      method: "POST",
      body: JSON.stringify({ senderUserId: appUser.id, receiverUserId: activeUserId, content }),
    });

    if (!response.ok) {
      setError(`Could not send message (${response.status}).`);
      return;
    }

    setDraft("");
    await loadConversation();
  };

  return (
    <AppShell searchValue={query} onSearchChange={setQuery} searchPlaceholder="Search conversations">
      <div className="grid min-h-[calc(100vh-120px)] gap-5 lg:grid-cols-[320px_minmax(0,1fr)]">
        <aside className="flex min-h-[360px] flex-col overflow-hidden rounded-3xl border border-white/20 bg-white/80 shadow-2xl backdrop-blur-xl lg:max-h-[calc(100vh-120px)]">
          <div className="border-b border-slate-200 p-4">
            <h1 className="text-lg font-semibold text-[#143b5d]">Messages</h1>
            <p className="text-sm text-slate-600">Private conversations</p>
          </div>

          <div className="flex-1 overflow-y-auto">
            {visibleUsers.map((user) => (
              <button
                key={user.id}
                type="button"
                onClick={() => setActiveUserId(user.id)}
                className={[
                  "flex w-full items-start gap-3 border-b border-slate-200 p-4 text-left transition",
                  activeUserId === user.id ? "bg-[#143b5d]/10" : "hover:bg-white",
                ].join(" ")}
              >
                <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full border border-slate-200 bg-white">
                  <Image src="/logo.png" alt={user.displayName ?? user.username} fill sizes="48px" className="object-contain p-2" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-semibold text-[#143b5d]">{user.displayName ?? user.username}</p>
                  <p className="text-xs text-slate-500">{user.accountType === "ORGANIZATION" ? "Organization" : "Individual"}</p>
                  <p className="mt-1 truncate text-sm text-slate-700">{user.email}</p>
                </div>
              </button>
            ))}
          </div>
        </aside>

        <section className="flex min-h-[560px] min-w-0 flex-col overflow-hidden rounded-3xl border border-white/20 bg-white/85 shadow-2xl backdrop-blur-xl lg:max-h-[calc(100vh-120px)]">
          <header className="flex items-center justify-between gap-3 border-b border-slate-200 px-4 py-3 sm:px-5">
            <div className="flex min-w-0 items-center gap-3">
              <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full border border-slate-200 bg-white">
                <Image src="/logo.png" alt={activeUser?.displayName ?? "Conversation"} fill sizes="48px" className="object-contain p-2" />
              </div>
              <div className="min-w-0">
                <h2 className="truncate font-semibold text-[#143b5d]">{activeUser?.displayName ?? "Select a conversation"}</h2>
                <p className="truncate text-sm text-slate-600">{activeUser?.email ?? "Choose a user to start messaging"}</p>
              </div>
            </div>
            <span className="shrink-0 rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-700">
              Live
            </span>
          </header>

          {error && <p className="m-4 rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p>}

          <div className="flex-1 space-y-3 overflow-y-auto p-4 sm:p-5">
            {loading && activeUser ? <p className="text-sm text-slate-500">Loading conversation...</p> : null}
            {!loading && messages.length === 0 ? <p className="text-sm text-slate-500">No messages yet.</p> : null}
            {messages.map((message) => {
              const fromMe = message.senderUserId === appUser?.id;
              return (
                <div key={message.messageId} className={`flex ${fromMe ? "justify-end" : "justify-start"}`}>
                  <div className={[
                    "max-w-[min(78%,540px)] rounded-2xl px-4 py-2 shadow-sm",
                    fromMe ? "bg-[#143b5d] text-white" : "bg-slate-100 text-slate-800",
                  ].join(" ")}
                  >
                    <p className="text-sm leading-6">{message.content}</p>
                    <p className={`mt-1 text-[10px] ${fromMe ? "text-blue-100" : "text-slate-500"}`}>
                      {formatRelativeTime(message.sentAt)}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>

          <form onSubmit={handleSend} className="flex gap-3 border-t border-slate-200 bg-white p-4">
            <label className="min-w-0 flex-1">
              <span className="sr-only">Write a message</span>
              <input
                type="text"
                value={draft}
                onChange={(event) => setDraft(event.target.value)}
                placeholder="Write a message..."
                className="h-11 w-full rounded-2xl border border-slate-200 px-4 text-sm text-slate-800 outline-none transition focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
              />
            </label>
            <button
              type="submit"
              disabled={!draft.trim() || !activeUser}
              className="h-11 rounded-2xl bg-[#143b5d] px-5 text-sm font-semibold text-white transition hover:bg-[#1d5485] disabled:cursor-not-allowed disabled:opacity-50"
            >
              Send
            </button>
          </form>
        </section>
      </div>
    </AppShell>
  );
}
