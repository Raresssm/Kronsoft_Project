"use client";

import Image from "next/image";
import { FormEvent, useMemo, useState } from "react";
import { AppShell } from "../components/AppShell";

type Thread = {
  id: number;
  name: string;
  role: string;
  preview: string;
  unread: number;
  avatar: string;
};

type ChatMessage = {
  id: number;
  fromMe: boolean;
  text: string;
  time: string;
};

const threads: Thread[] = [
  {
    id: 1,
    name: "Alex Radu",
    role: "Software Engineer",
    preview: "Perfect, let's sync tomorrow at 10.",
    unread: 2,
    avatar: "/agora.jpg",
  },
  {
    id: 2,
    name: "CodeWave SRL",
    role: "Organization",
    preview: "We reviewed your profile and want to connect.",
    unread: 0,
    avatar: "/logo.png",
  },
  {
    id: 3,
    name: "Ioana Pop",
    role: "UX Designer",
    preview: "Can you share the API docs for messaging?",
    unread: 1,
    avatar: "/agora_campus.jpg",
  },
];

const initialMessages: Record<number, ChatMessage[]> = {
  1: [
    { id: 1, fromMe: false, text: "Hey! Did you finish the backend endpoint?", time: "09:14" },
    { id: 2, fromMe: true, text: "Yes, it's ready. I also added validation.", time: "09:17" },
    { id: 3, fromMe: false, text: "Perfect, let's sync tomorrow at 10.", time: "09:20" },
  ],
  2: [
    { id: 1, fromMe: false, text: "Hello! Thanks for applying to our internship.", time: "Yesterday" },
    { id: 2, fromMe: true, text: "Thank you. Happy to discuss details.", time: "Yesterday" },
  ],
  3: [
    { id: 1, fromMe: false, text: "Can you share the API docs for messaging?", time: "08:55" },
    { id: 2, fromMe: true, text: "Sure, I'll send the OpenAPI link now.", time: "09:02" },
  ],
};

export default function Messages() {
  const [activeThreadId, setActiveThreadId] = useState<number>(threads[0].id);
  const [draft, setDraft] = useState("");
  const [query, setQuery] = useState("");
  const [messages, setMessages] = useState<Record<number, ChatMessage[]>>(initialMessages);

  const normalizedQuery = query.trim().toLowerCase();

  const visibleThreads = useMemo(() => {
    if (!normalizedQuery) {
      return threads;
    }

    return threads.filter((thread) =>
      [thread.name, thread.role, thread.preview].join(" ").toLowerCase().includes(normalizedQuery),
    );
  }, [normalizedQuery]);

  const activeThread = useMemo(
    () => threads.find((thread) => thread.id === activeThreadId) ?? threads[0],
    [activeThreadId],
  );

  const activeMessages = messages[activeThread.id] ?? [];

  const handleSend = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const text = draft.trim();
    if (!text) {
      return;
    }

    setMessages((currentMessages) => ({
      ...currentMessages,
      [activeThread.id]: [
        ...(currentMessages[activeThread.id] ?? []),
        { id: Date.now(), fromMe: true, text, time: "Now" },
      ],
    }));
    setDraft("");
  };

  return (
    <AppShell
      searchValue={query}
      onSearchChange={setQuery}
      searchPlaceholder="Search conversations"
    >
      <div className="grid min-h-[calc(100vh-120px)] gap-5 lg:grid-cols-[320px_minmax(0,1fr)]">
        <aside className="flex min-h-[360px] flex-col overflow-hidden rounded-3xl border border-white/20 bg-white/80 shadow-2xl backdrop-blur-xl lg:max-h-[calc(100vh-120px)]">
          <div className="border-b border-slate-200 p-4">
            <h1 className="text-lg font-semibold text-[#143b5d]">Messages</h1>
            <p className="text-sm text-slate-600">Private conversations</p>
          </div>

          <div className="flex-1 overflow-y-auto">
            {visibleThreads.map((thread) => (
              <button
                key={thread.id}
                type="button"
                onClick={() => setActiveThreadId(thread.id)}
                className={[
                  "flex w-full items-start gap-3 border-b border-slate-200 p-4 text-left transition",
                  activeThreadId === thread.id ? "bg-[#143b5d]/10" : "hover:bg-white",
                ].join(" ")}
              >
                <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full border border-slate-200 bg-white">
                  <Image src={thread.avatar} alt={thread.name} fill sizes="48px" className="object-cover" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <p className="truncate text-sm font-semibold text-[#143b5d]">{thread.name}</p>
                    {thread.unread > 0 && (
                      <span className="rounded-full bg-[#143b5d] px-2 py-0.5 text-[10px] font-bold text-white">
                        {thread.unread}
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500">{thread.role}</p>
                  <p className="mt-1 truncate text-sm text-slate-700">{thread.preview}</p>
                </div>
              </button>
            ))}
          </div>
        </aside>

        <section className="flex min-h-[560px] min-w-0 flex-col overflow-hidden rounded-3xl border border-white/20 bg-white/85 shadow-2xl backdrop-blur-xl lg:max-h-[calc(100vh-120px)]">
          <header className="flex items-center justify-between gap-3 border-b border-slate-200 px-4 py-3 sm:px-5">
            <div className="flex min-w-0 items-center gap-3">
              <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-full border border-slate-200 bg-white">
                <Image src={activeThread.avatar} alt={activeThread.name} fill sizes="48px" className="object-cover" />
              </div>
              <div className="min-w-0">
                <h2 className="truncate font-semibold text-[#143b5d]">{activeThread.name}</h2>
                <p className="truncate text-sm text-slate-600">{activeThread.role}</p>
              </div>
            </div>
            <span className="shrink-0 rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-700">
              Online
            </span>
          </header>

          <div className="flex-1 space-y-3 overflow-y-auto p-4 sm:p-5">
            {activeMessages.map((message) => (
              <div
                key={message.id}
                className={`flex ${message.fromMe ? "justify-end" : "justify-start"}`}
              >
                <div
                  className={[
                    "max-w-[min(78%,540px)] rounded-2xl px-4 py-2 shadow-sm",
                    message.fromMe ? "bg-[#143b5d] text-white" : "bg-slate-100 text-slate-800",
                  ].join(" ")}
                >
                  <p className="text-sm leading-6">{message.text}</p>
                  <p className={`mt-1 text-[10px] ${message.fromMe ? "text-blue-100" : "text-slate-500"}`}>
                    {message.time}
                  </p>
                </div>
              </div>
            ))}
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
              disabled={!draft.trim()}
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
