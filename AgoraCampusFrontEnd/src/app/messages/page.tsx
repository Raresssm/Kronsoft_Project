"use client";

import React, { FormEvent, useMemo, useState } from "react";
import Image from "next/image";
import Link from "next/link";

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

const THREADS: Thread[] = [
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

const MESSAGES: Record<number, ChatMessage[]> = {
  1: [
    { id: 1, fromMe: false, text: "Hey! Did you finish the backend endpoint?", time: "09:14" },
    { id: 2, fromMe: true, text: "Yes, it's ready. I also added validation.", time: "09:17" },
    { id: 3, fromMe: false, text: "Perfect, let's sync tomorrow at 10.", time: "09:20" },
  ],
  2: [
    { id: 1, fromMe: false, text: "Hello! Thanks for applying to our internship.", time: "Yesterday" },
    { id: 2, fromMe: true, text: "Thank you! Happy to discuss details.", time: "Yesterday" },
  ],
  3: [
    { id: 1, fromMe: false, text: "Can you share the API docs for messaging?", time: "08:55" },
    { id: 2, fromMe: true, text: "Sure, I'll send the OpenAPI link now.", time: "09:02" },
  ],
};

export default function Messages() {
  const [activeThreadId, setActiveThreadId] = useState<number>(THREADS[0].id);
  const [draft, setDraft] = useState<string>("");

  const activeThread = useMemo(
    () => THREADS.find((thread) => thread.id === activeThreadId) ?? THREADS[0],
    [activeThreadId],
  );

  const activeMessages = useMemo(
    () => MESSAGES[activeThread.id] ?? [],
    [activeThread.id],
  );

  const handleSend = (e: FormEvent) => {
    e.preventDefault();
    if (!draft.trim()) return;
    setDraft("");
  };

  return (
    <div className="relative h-screen w-full bg-[white] overflowX-hidden flex flex-column">
      <Image src="/blur_cover.png" alt="profile" fill className="object-cover" />

      <div className="relative w-full h-[50px] bg-white/20 border border-white/20 shadow-2xl flex items-center px-4">
        <div className="relative w-[100px] h-full">
          <Image src="/logo.png" alt="Agora" fill className="object-contain" priority />
        </div>

        <div className="relative w-[300px]">
          <input
            type="text"
            placeholder="Search..."
            className="w-full pl-10 pr-10 py-2 rounded-xl bg-white/30 border-b border-gray-300/60 text-black outline-none"
          />

          <div className="absolute left-3 top-1/2 cursor-pointer -translate-y-1/2 text-[#143b5d]">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#143b5d"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="w-5 h-5"
            >
              <circle cx="11" cy="11" r="7" />
              <line x1="16.65" y1="16.65" x2="21" y2="21" />
            </svg>
          </div>

          <Link href="/feed">
            <div className="absolute left-100 top-1/2 cursor-pointer -translate-y-1/2 text-[#143b5d]">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#143b5d"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="w-6 h-6 text-blue-500"
              >
                <path d="M3 10.5L12 3l9 7.5" />
                <path d="M5 9.5V21h14V9.5" />
                <path d="M10 21v-6h4v6" />
              </svg>
              <span className="text-xs mt-1">Home</span>
            </div>
          </Link>

          <Link href="/messages">
            <div className="absolute left-120 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#143b5d"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="w-6 h-6 text-blue-500"
              >
                <path d="M21 15a4 4 0 0 1-4 4H8l-5 3 2-4a4 4 0 0 1-2-3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
              </svg>
              <span className="text-xs mt-1">Messages</span>
            </div>
          </Link>

          <Link href="/jobs">
            <div className="absolute left-140 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#143b5d"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="w-6 h-6 text-blue-500"
              >
                <rect x="3" y="7" width="18" height="14" rx="2" ry="2" />
                <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                <path d="M3 13h18" />
              </svg>

              <span className="text-xs mt-1">Jobs</span>
            </div>
          </Link>
          <Link href="/alerts">
            <div className="absolute left-160 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#143b5d"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="w-6 h-6 text-blue-500"
              >
                <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 7h18s-3 0-3-7" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
              <span className="text-xs mt-1">Alerts</span>
            </div>
          </Link>

          <Link href="/network">
            <div className="absolute left-180 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#143b5d"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="w-6 h-6 text-blue-500"
              >
                <circle cx="9" cy="8" r="3" />
                <path d="M3 20c0-3.3 2.7-6 6-6" />
                <circle cx="17" cy="10" r="3" />
                <path d="M13 20c0-3.3 2.7-6 6-6" />
              </svg>
              <span className="text-xs mt-1">Network</span>
            </div>
          </Link>
          <Link href="/individual_profile">
            <div className="absolute left-200 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#143b5d"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="w-6 h-6"
              >
                <circle cx="12" cy="12" r="10" />
                <circle cx="12" cy="10" r="3" />
                <path d="M8 18c0-2.2 1.8-4 4-4s4 1.8 4 4" />
              </svg>

              <span className="text-xs mt-1">Profile</span>
            </div>
          </Link>
        </div>
      </div>

      <div className="w-[980px] h-[660px] absolute mt-[6%] ml-[10%] rounded-3xl bg-white border border-white/20 shadow-2xl backdrop-blur-xl overflow-hidden">
        <div className="h-full flex">
          <aside className="w-[310px] h-full bg-white/65 border-r border-white/50">
            <div className="p-5 border-b border-gray-200/70">
              <h2 className="text-[#143b5d] text-lg font-semibold tracking-wide">Messages</h2>
              <p className="text-xs text-gray-600 mt-1">Private conversations</p>
            </div>

            <div className="p-3 border-b border-gray-200/70">
              <input
                type="text"
                placeholder="Search messages"
                className="w-full rounded-xl bg-white/70 border border-gray-200 px-3 py-2 text-sm text-gray-700 outline-none"
              />
            </div>

            <div className="h-[calc(100%-121px)] overflow-y-auto">
              {THREADS.map((thread) => (
                <button
                  key={thread.id}
                  type="button"
                  onClick={() => setActiveThreadId(thread.id)}
                  className={`w-full flex items-start gap-3 p-4 border-b border-gray-200/60 text-left transition ${
                    activeThreadId === thread.id ? "bg-[#143b5d]/10" : "hover:bg-white/70"
                  }`}
                >
                  <div className="relative w-11 h-11 rounded-full overflow-hidden border border-gray-300/70 shrink-0">
                    <Image src={thread.avatar} alt={thread.name} fill className="object-cover" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <p className="text-sm font-semibold text-[#143b5d] truncate">{thread.name}</p>
                      {thread.unread > 0 ? (
                        <span className="text-[10px] font-bold bg-[#143b5d] text-white rounded-full px-2 py-0.5">
                          {thread.unread}
                        </span>
                      ) : null}
                    </div>
                    <p className="text-[11px] text-gray-500">{thread.role}</p>
                    <p className="text-xs text-gray-700 truncate mt-1">{thread.preview}</p>
                  </div>
                </button>
              ))}
            </div>
          </aside>

          <section className="flex-1 h-full flex flex-col bg-white/45">
            <header className="h-[76px] px-6 border-b border-gray-200/70 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="relative w-11 h-11 rounded-full overflow-hidden border border-white/70 shadow">
                  <Image src={activeThread.avatar} alt={activeThread.name} fill className="object-cover" />
                </div>
                <div>
                  <h3 className="text-[#143b5d] font-semibold">{activeThread.name}</h3>
                  <p className="text-xs text-gray-600">{activeThread.role}</p>
                </div>
              </div>
              <span className="text-xs text-emerald-700 font-medium">Online</span>
            </header>

            <div className="flex-1 overflow-y-auto p-6 space-y-3">
              {activeMessages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${message.fromMe ? "justify-end" : "justify-start"}`}
                >
                  <div
                    className={`max-w-[70%] rounded-2xl px-4 py-2 shadow ${
                      message.fromMe
                        ? "bg-[#143b5d] text-white rounded-br-md"
                        : "bg-white text-gray-800 rounded-bl-md"
                    }`}
                  >
                    <p className="text-sm">{message.text}</p>
                    <p className={`text-[10px] mt-1 ${message.fromMe ? "text-blue-100" : "text-gray-500"}`}>
                      {message.time}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            <form
              onSubmit={handleSend}
              className="h-[78px] border-t border-gray-200/70 px-4 flex items-center gap-3 bg-white/65"
            >
              <input
                type="text"
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                placeholder="Write a message..."
                className="flex-1 rounded-xl bg-white border border-gray-200 px-4 py-2 text-sm text-gray-700 outline-none"
              />
              <button
                type="submit"
                className="rounded-xl bg-[#143b5d] text-white text-sm font-semibold px-5 py-2 hover:bg-[#1d5485] transition"
              >
                Send
              </button>
            </form>
          </section>
        </div>
      </div>
    </div>
  );
}
