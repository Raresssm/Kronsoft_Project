"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { AppShell } from "../components/AppShell";
import type {
  AppUserSummary,
  ConnectionResponse,
  MessageResponse,
  OpportunityApplicationResponse,
  OpportunityResponse,
} from "../lib/api-types";
import { formatRelativeTime } from "../lib/api-types";
import { useAuth } from "../lib/auth";

type AlertType = "message" | "job" | "network";
type AlertFilter = "all" | "unread" | AlertType;

type AlertItem = {
  id: string;
  type: AlertType;
  title: string;
  body: string;
  time: string;
  timestamp: string;
  read: boolean;
  action?: {
    label: string;
    href: string;
  };
  messageId?: number;
};

const filters: { value: AlertFilter; label: string }[] = [
  { value: "all", label: "All" },
  { value: "unread", label: "Unread" },
  { value: "message", label: "Messages" },
  { value: "job", label: "Jobs" },
  { value: "network", label: "Network" },
];

export default function Alerts() {
  const { appUser, apiFetch } = useAuth();
  const [alerts, setAlerts] = useState<AlertItem[]>([]);
  const [localReadIds, setLocalReadIds] = useState<Set<string>>(new Set());
  const [activeFilter, setActiveFilter] = useState<AlertFilter>("all");
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadAlerts = useCallback(async () => {
    if (!appUser) return;

    setLoading(true);
    setError("");

    try {
      const [usersResponse, connectionsResponse, messagesResponse, opportunitiesResponse, myApplicationsResponse] =
        await Promise.all([
          apiFetch("/api/users"),
          apiFetch(`/api/connections/incoming/${appUser.id}/pending?actingUserId=${appUser.id}`),
          apiFetch(`/api/messages/incoming/${appUser.id}/unread?actingUserId=${appUser.id}`),
          apiFetch(`/api/opportunities?actingUserId=${appUser.id}`),
          apiFetch(`/api/opportunities/applications/users/${appUser.id}?actingUserId=${appUser.id}`),
        ]);

      if (!usersResponse.ok) throw new Error(`Could not load users (${usersResponse.status}).`);
      if (!connectionsResponse.ok) throw new Error(`Could not load connection alerts (${connectionsResponse.status}).`);
      if (!messagesResponse.ok) throw new Error(`Could not load message alerts (${messagesResponse.status}).`);
      if (!opportunitiesResponse.ok) throw new Error(`Could not load job alerts (${opportunitiesResponse.status}).`);
      if (!myApplicationsResponse.ok) throw new Error(`Could not load application alerts (${myApplicationsResponse.status}).`);

      const users = (await usersResponse.json()) as AppUserSummary[];
      const userNames = Object.fromEntries(users.map((user) => [user.id, user.displayName || user.username]));
      const connections = (await connectionsResponse.json()) as ConnectionResponse[];
      const messages = (await messagesResponse.json()) as MessageResponse[];
      const opportunities = (await opportunitiesResponse.json()) as OpportunityResponse[];
      const myApplications = (await myApplicationsResponse.json()) as OpportunityApplicationResponse[];
      const ownedOpportunities = opportunities.filter((opportunity) => opportunity.postedByUserId === appUser.id);

      const applicationsForOwnedOpportunities = await Promise.all(
        ownedOpportunities.map(async (opportunity) => {
          const response = await apiFetch(
            `/api/opportunities/${opportunity.opportunityId}/applications?actingUserId=${appUser.id}`,
          );
          if (!response.ok) return [];
          const applications = (await response.json()) as OpportunityApplicationResponse[];
          return applications.map((application) => ({ application, opportunity }));
        }),
      );

      const opportunityById = Object.fromEntries(
        opportunities.map((opportunity) => [opportunity.opportunityId, opportunity]),
      );

      const nextAlerts: AlertItem[] = [
        ...messages.map((message) =>
          withLocalRead(
            {
              id: `message-${message.messageId}`,
              type: "message" as const,
              title: `New message from ${userNames[message.senderUserId] ?? "Unknown user"}`,
              body: message.content,
              time: formatRelativeTime(message.sentAt),
              timestamp: message.sentAt,
              read: Boolean(message.acknowledged),
              action: { label: "Open messages", href: "/messages" },
              messageId: message.messageId,
            },
            localReadIds,
          ),
        ),
        ...connections.map((connection) =>
          withLocalRead(
            {
              id: `connection-${connection.connectionId}`,
              type: "network" as const,
              title: `${userNames[connection.requesterUserId] ?? "Someone"} sent a connection request`,
              body: "Review the request from your network page.",
              time: formatRelativeTime(connection.createdAt),
              timestamp: connection.createdAt,
              read: false,
              action: { label: "Open network", href: "/network" },
            },
            localReadIds,
          ),
        ),
        ...myApplications.map((application) => {
          const opportunity = opportunityById[application.opportunityId];
          return withLocalRead(
            {
              id: `my-application-${application.applicationId}-${application.status}`,
              type: "job" as const,
              title: `Application ${application.status.toLowerCase()}`,
              body: opportunity
                ? `${opportunity.title} is currently ${application.status.toLowerCase()}.`
                : `Your application is currently ${application.status.toLowerCase()}.`,
              time: formatRelativeTime(application.appliedAt),
              timestamp: application.appliedAt,
              read: application.status === "PENDING",
              action: { label: "Open jobs", href: "/jobs" },
            },
            localReadIds,
          );
        }),
        ...applicationsForOwnedOpportunities.flat().map(({ application, opportunity }) =>
          withLocalRead(
            {
              id: `owned-application-${application.applicationId}`,
              type: "job" as const,
              title: `${application.applicantUsername} applied`,
              body: `${application.applicantUsername} applied to ${opportunity.title}.`,
              time: formatRelativeTime(application.appliedAt),
              timestamp: application.appliedAt,
              read: application.status !== "PENDING",
              action: { label: "Review application", href: "/jobs" },
            },
            localReadIds,
          ),
        ),
      ].sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

      setAlerts(nextAlerts);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load alerts.");
    } finally {
      setLoading(false);
    }
  }, [apiFetch, appUser, localReadIds]);

  useEffect(() => {
    queueMicrotask(() => void loadAlerts());
  }, [loadAlerts]);

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

  const markRead = async (alert: AlertItem) => {
    if (alert.messageId && appUser) {
      await apiFetch(`/api/messages/${alert.messageId}/read?actingUserId=${appUser.id}`, {
        method: "PATCH",
      }).catch(() => undefined);
    }

    setLocalReadIds((current) => new Set(current).add(alert.id));
    setAlerts((currentAlerts) =>
      currentAlerts.map((currentAlert) =>
        currentAlert.id === alert.id ? { ...currentAlert, read: true } : currentAlert,
      ),
    );
  };

  const markAllRead = async () => {
    await Promise.all(visibleAlerts.filter((alert) => !alert.read).map(markRead));
  };

  const clearRead = () => {
    setAlerts((currentAlerts) => currentAlerts.filter((alert) => !alert.read));
  };

  return (
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
            <button
              type="button"
              onClick={loadAlerts}
              className="rounded-full bg-[#143b5d]/10 px-3 py-1 text-xs font-semibold text-[#143b5d] transition hover:bg-[#143b5d]/15"
            >
              Refresh
            </button>
          </div>

          <div className="mt-4 space-y-3">
            {loading && <EmptyState title="Loading alerts" body="Checking messages, network, and jobs." />}

            {error && (
              <div className="rounded-3xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
                {error}
              </div>
            )}

            {!loading &&
              visibleAlerts.map((alert) => (
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
                    <div className="flex shrink-0 flex-wrap gap-2">
                      {alert.action && (
                        <a
                          href={alert.action.href}
                          className="h-10 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                        >
                          {alert.action.label}
                        </a>
                      )}
                      {!alert.read && (
                        <button
                          type="button"
                          onClick={() => void markRead(alert)}
                          className="h-10 rounded-full bg-[#143b5d] px-4 text-sm font-semibold text-white hover:bg-[#1d5485]"
                        >
                          Mark read
                        </button>
                      )}
                    </div>
                  </div>
                </article>
              ))}

            {!loading && visibleAlerts.length === 0 && (
              <EmptyState title="No alerts found" body="There are no matching backend alerts right now." />
            )}
          </div>
        </section>
      </div>
    </AppShell>
  );
}

function withLocalRead(alert: AlertItem, localReadIds: Set<string>) {
  return localReadIds.has(alert.id) ? { ...alert, read: true } : alert;
}

function EmptyState({ title, body }: { title: string; body: string }) {
  return (
    <div className="rounded-3xl border border-dashed border-slate-300 bg-white/80 px-6 py-12 text-center">
      <h2 className="font-semibold text-[#143b5d]">{title}</h2>
      <p className="mt-1 text-sm text-slate-600">{body}</p>
    </div>
  );
}
