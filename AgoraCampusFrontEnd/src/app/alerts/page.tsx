"use client";

import { useMemo, useState } from "react";
import { RequireAuth } from "@/components/RequireAuth";
import { AppShell } from "../components/AppShell";

type AlertType = "message" | "job" | "network" | "event";
type AlertFilter = "all" | "unread" | AlertType;

type AlertItem = {
  id: number;
  type: AlertType;
  title: string;
  body: string;
  time: string;
  read: boolean;
};

const initialAlerts: AlertItem[] = [
  {
    id: 1,
    type: "message",
    title: "New message from Alex Radu",
    body: "Perfect, let's sync tomorrow at 10.",
    time: "5 min ago",
    read: false,
  },
  {
    id: 2,
    type: "job",
    title: "Frontend Internship matched your interests",
    body: "Kronsoft is looking for React and TypeScript skills.",
    time: "28 min ago",
    read: false,
  },
  {
    id: 3,
    type: "network",
    title: "Cristina Barbu sent a connection request",
    body: "You have 7 mutual connections.",
    time: "1 hour ago",
    read: true,
  },
  {
    id: 4,
    type: "event",
    title: "Portfolio review starts soon",
    body: "Bring one project and one question for mentors.",
    time: "Yesterday",
    read: true,
  },
];

const filters: { value: AlertFilter; label: string }[] = [
  { value: "all", label: "All" },
  { value: "unread", label: "Unread" },
  { value: "message", label: "Messages" },
  { value: "job", label: "Jobs" },
  { value: "network", label: "Network" },
  { value: "event", label: "Events" },
];

export default function Alerts() {
  const [alerts, setAlerts] = useState<AlertItem[]>(initialAlerts);
  const [activeFilter, setActiveFilter] = useState<AlertFilter>("all");
  const [query, setQuery] = useState("");

  const normalizedQuery = query.trim().toLowerCase();

  const visibleAlerts = useMemo(() => {
    return alerts.filter((alert) => {
      const matchesFilter =
        activeFilter === "all" ||
        (activeFilter === "unread" ? !alert.read : alert.type === activeFilter);
      const matchesSearch =
        !normalizedQuery ||
        [alert.title, alert.body, alert.type].join(" ").toLowerCase().includes(normalizedQuery);

      return matchesFilter && matchesSearch;
    });
  }, [activeFilter, alerts, normalizedQuery]);

  const unreadCount = alerts.filter((alert) => !alert.read).length;

  const markAllRead = () => {
    setAlerts((currentAlerts) => currentAlerts.map((alert) => ({ ...alert, read: true })));
  };

  const clearRead = () => {
    setAlerts((currentAlerts) => currentAlerts.filter((alert) => !alert.read));
  };

  const toggleRead = (id: number) => {
    setAlerts((currentAlerts) =>
      currentAlerts.map((alert) => (alert.id === id ? { ...alert, read: !alert.read } : alert)),
    );
  };

  return (
    <RequireAuth>
    <AppShell
      searchValue={query}
      onSearchChange={setQuery}
      searchPlaceholder="Search alerts"
    >
      <div className="grid gap-5 lg:grid-cols-[280px_minmax(0,1fr)]">
        <aside className="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h1 className="text-lg font-semibold text-[#143b5d]">Alerts</h1>
            <p className="text-sm text-slate-700">{unreadCount} unread notifications</p>

            <div className="mt-4 flex flex-col gap-2">
              <button
                type="button"
                onClick={markAllRead}
                className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white hover:bg-[#1d5485]"
              >
                Mark all read
              </button>
              <button
                type="button"
                onClick={clearRead}
                className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
              >
                Clear read
              </button>
            </div>
          </section>

          <section className="rounded-3xl border border-white/20 bg-white/80 p-3 shadow-2xl backdrop-blur-xl">
            <div className="grid grid-cols-2 gap-2">
              {filters.map((filter) => (
                <button
                  key={filter.value}
                  type="button"
                  onClick={() => setActiveFilter(filter.value)}
                  className={[
                    "rounded-full px-3 py-2 text-sm font-semibold transition",
                    activeFilter === filter.value
                      ? "bg-[#143b5d] text-white"
                      : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                  ].join(" ")}
                >
                  {filter.label}
                </button>
              ))}
            </div>
          </section>
        </aside>

        <section className="min-w-0 rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl sm:p-5">
          <div className="flex flex-col gap-2 border-b border-slate-200 pb-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-lg font-semibold text-[#143b5d]">Notification Center</h2>
              <p className="text-sm text-slate-600">{visibleAlerts.length} alerts visible</p>
            </div>
            <span className="rounded-full bg-[#143b5d]/10 px-3 py-1 text-xs font-semibold text-[#143b5d]">
              {activeFilter === "all"
                ? "All alerts"
                : filters.find((filter) => filter.value === activeFilter)?.label}
            </span>
          </div>

          <div className="mt-4 space-y-3">
            {visibleAlerts.map((alert) => (
              <article
                key={alert.id}
                className={[
                  "rounded-3xl border p-4 shadow-sm transition",
                  alert.read ? "border-slate-200 bg-white" : "border-[#143b5d]/20 bg-[#143b5d]/5",
                ].join(" ")}
              >
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="rounded-full bg-white px-2.5 py-1 text-xs font-semibold capitalize text-[#143b5d]">
                        {alert.type}
                      </span>
                      {!alert.read && (
                        <span className="rounded-full bg-[#143b5d] px-2.5 py-1 text-xs font-semibold text-white">
                          New
                        </span>
                      )}
                    </div>
                    <h3 className="mt-3 font-semibold text-[#143b5d]">{alert.title}</h3>
                    <p className="mt-1 text-sm leading-6 text-slate-700">{alert.body}</p>
                    <p className="mt-2 text-xs text-slate-500">{alert.time}</p>
                  </div>
                  <button
                    type="button"
                    onClick={() => toggleRead(alert.id)}
                    className="h-10 shrink-0 rounded-full border border-slate-200 bg-white px-4 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                  >
                    {alert.read ? "Mark unread" : "Mark read"}
                  </button>
                </div>
              </article>
            ))}

            {visibleAlerts.length === 0 && (
              <div className="rounded-3xl border border-dashed border-slate-300 bg-white/80 px-6 py-12 text-center">
                <h2 className="font-semibold text-[#143b5d]">No alerts found</h2>
                <p className="mt-1 text-sm text-slate-600">Try a different filter or search term.</p>
              </div>
            )}
          </div>
        </section>
      </div>
    </AppShell>
    </RequireAuth>
  );
}
