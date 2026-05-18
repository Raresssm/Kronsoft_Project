"use client";

import Image from "next/image";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { AppShell } from "../components/AppShell";
import { AppUserSummary, ConnectionResponse } from "../lib/api-types";
import { useAuth } from "../lib/auth";

type NetworkTab = "requests" | "connections" | "discover";
type Relationship = "none" | "pending" | "connected" | "incoming";

const tabLabels: Record<NetworkTab, string> = {
  requests: "Requests",
  connections: "Connections",
  discover: "Discover",
};

export default function Network() {
  const { appUser, apiFetch } = useAuth();
  const [activeTab, setActiveTab] = useState<NetworkTab>("discover");
  const [query, setQuery] = useState("");
  const [users, setUsers] = useState<AppUserSummary[]>([]);
  const [connections, setConnections] = useState<ConnectionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadNetwork = useCallback(async () => {
    if (!appUser) return;

    setLoading(true);
    setError("");

    try {
      const [usersResponse, connectionsResponse] = await Promise.all([
        apiFetch("/api/users"),
        apiFetch(`/api/connections/users/${appUser.id}?actingUserId=${appUser.id}`),
      ]);

      if (!usersResponse.ok) throw new Error(`Could not load users (${usersResponse.status}).`);
      if (!connectionsResponse.ok) throw new Error(`Could not load connections (${connectionsResponse.status}).`);

      setUsers(((await usersResponse.json()) as AppUserSummary[]).filter((user) => user.id !== appUser.id));
      setConnections((await connectionsResponse.json()) as ConnectionResponse[]);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load network.");
    } finally {
      setLoading(false);
    }
  }, [apiFetch, appUser]);

  useEffect(() => {
    queueMicrotask(() => void loadNetwork());
  }, [loadNetwork]);

  const connectionFor = useCallback(
    (userId: number) =>
      connections.find(
        (connection) =>
          connection.requesterUserId === userId ||
          connection.receiverUserId === userId,
      ),
    [connections],
  );

  const relationshipFor = useCallback(
    (userId: number): Relationship => {
      const connection = connectionFor(userId);
      if (!connection) return "none";
      if (connection.status === "ACCEPTED") return "connected";
      if (connection.status === "PENDING" && connection.receiverUserId === appUser?.id) return "incoming";
      if (connection.status === "PENDING") return "pending";
      return "none";
    },
    [appUser?.id, connectionFor],
  );

  const normalizedQuery = query.trim().toLowerCase();
  const visibleUsers = useMemo(() => {
    return users.filter((user) => {
      const relationship = relationshipFor(user.id);
      const matchesTab =
        activeTab === "requests"
          ? relationship === "incoming"
          : activeTab === "connections"
            ? relationship === "connected"
            : relationship !== "connected";
      const matchesSearch =
        !normalizedQuery ||
        [user.displayName, user.username, user.email, user.accountType].join(" ").toLowerCase().includes(normalizedQuery);

      return matchesTab && matchesSearch;
    });
  }, [activeTab, normalizedQuery, relationshipFor, users]);

  const createConnection = async (receiverUserId: number) => {
    if (!appUser) return;

    const response = await apiFetch(`/api/connections?actingUserId=${appUser.id}`, {
      method: "POST",
      body: JSON.stringify({ requesterUserId: appUser.id, receiverUserId }),
    });

    if (!response.ok) {
      setError(`Could not send connection request (${response.status}).`);
      return;
    }

    await loadNetwork();
  };

  const updateConnection = async (connectionId: number, status: "ACCEPTED" | "REJECTED") => {
    if (!appUser) return;

    const response = await apiFetch(`/api/connections/${connectionId}/status?actingUserId=${appUser.id}`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });

    if (!response.ok) {
      setError(`Could not update connection (${response.status}).`);
      return;
    }

    await loadNetwork();
  };

  const deleteConnection = async (connectionId: number) => {
    if (!appUser) return;

    const response = await apiFetch(`/api/connections/${connectionId}?actingUserId=${appUser.id}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      setError(`Could not remove connection (${response.status}).`);
      return;
    }

    await loadNetwork();
  };

  const totalConnections = connections.filter((connection) => connection.status === "ACCEPTED").length;
  const totalPending = connections.filter((connection) => connection.status === "PENDING" && connection.requesterUserId === appUser?.id).length;
  const totalRequests = connections.filter((connection) => connection.status === "PENDING" && connection.receiverUserId === appUser?.id).length;

  return (
    <AppShell searchValue={query} onSearchChange={setQuery} searchPlaceholder="Search people, skills, company">
      <div className="grid gap-5 lg:grid-cols-[380px_minmax(0,1fr)]">
        <aside className="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h1 className="text-lg font-semibold text-[#143b5d]">Network</h1>
            <p className="text-sm text-slate-700">Manage professional relationships.</p>

            <div className="mt-4 grid grid-cols-3 gap-3 text-center">
              <Stat label="Connected" value={totalConnections} />
              <Stat label="Pending" value={totalPending} />
              <Stat label="Requests" value={totalRequests} />
            </div>
          </section>

          <section className="rounded-3xl border border-white/20 bg-white/80 p-3 shadow-2xl backdrop-blur-xl">
            <div className="flex gap-2">
              {(Object.keys(tabLabels) as NetworkTab[]).map((tab) => (
                <button
                  key={tab}
                  type="button"
                  onClick={() => setActiveTab(tab)}
                  className={[
                    "min-h-11 min-w-0 flex-1 rounded-full px-4 py-2 text-center text-sm font-semibold leading-tight transition",
                    activeTab === tab ? "bg-[#143b5d] text-white" : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                  ].join(" ")}
                >
                  <span className="block whitespace-nowrap">{tabLabels[tab]}</span>
                </button>
              ))}
            </div>
          </section>
        </aside>

        <section className="min-w-0 rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl sm:p-5">
          <div className="flex flex-col gap-2 border-b border-slate-200 pb-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-lg font-semibold text-[#143b5d]">{tabLabels[activeTab]}</h2>
              <p className="text-sm text-slate-600">{loading ? "Loading..." : `${visibleUsers.length} profiles visible`}</p>
            </div>
            <span className="rounded-full bg-[#143b5d]/10 px-3 py-1 text-xs font-semibold text-[#143b5d]">
              {normalizedQuery ? `Filtered by "${query}"` : "All results"}
            </span>
          </div>

          {error && <p className="mt-4 rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p>}

          <div className="mt-4 grid gap-4 xl:grid-cols-2">
            {visibleUsers.map((user) => {
              const connection = connectionFor(user.id);
              const relationship = relationshipFor(user.id);

              return (
                <article key={user.id} className="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm">
                  <div className="flex min-w-0 items-start justify-between gap-3">
                    <div className="flex min-w-0 gap-3">
                      <div className="relative h-14 w-14 shrink-0 overflow-hidden rounded-full border border-slate-200 bg-white">
                        <Image src="/logo.png" alt={user.displayName ?? user.username} fill sizes="56px" className="object-contain p-2" />
                      </div>
                      <div className="min-w-0">
                        <h3 className="truncate font-semibold text-[#143b5d]">{user.displayName ?? user.username}</h3>
                        <p className="text-sm text-slate-700">{user.accountType === "ORGANIZATION" ? "Organization" : "Individual"}</p>
                        <p className="text-xs text-slate-500">{user.email}</p>
                      </div>
                    </div>
                    {relationship !== "none" && (
                      <span className="shrink-0 rounded-full bg-[#143b5d]/10 px-2 py-1 text-[11px] font-semibold text-[#143b5d]">
                        {relationship === "connected" ? "Connected" : relationship === "incoming" ? "Request" : "Pending"}
                      </span>
                    )}
                  </div>

                  <div className="mt-4 flex flex-wrap gap-2">
                    {relationship === "incoming" && connection ? (
                      <>
                        <button type="button" onClick={() => updateConnection(connection.connectionId, "ACCEPTED")} className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white hover:bg-[#1d5485]">
                          Accept
                        </button>
                        <button type="button" onClick={() => updateConnection(connection.connectionId, "REJECTED")} className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">
                          Ignore
                        </button>
                      </>
                    ) : relationship === "connected" ? (
                      <Link href={`/messages?userId=${user.id}`} className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white hover:bg-[#1d5485]">
                        Message
                      </Link>
                    ) : relationship === "pending" && connection ? (
                      <button type="button" onClick={() => deleteConnection(connection.connectionId)} className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">
                        Withdraw
                      </button>
                    ) : (
                      <button type="button" onClick={() => createConnection(user.id)} className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white hover:bg-[#1d5485]">
                        Connect
                      </button>
                    )}
                  </div>
                </article>
              );
            })}
          </div>

          {!loading && visibleUsers.length === 0 && (
            <div className="mt-4 rounded-3xl border border-dashed border-slate-300 bg-white/80 px-6 py-12 text-center">
              <h2 className="font-semibold text-[#143b5d]">No profiles found</h2>
              <p className="mt-1 text-sm text-slate-600">Try a different tab or create another account to connect with.</p>
            </div>
          )}
        </section>
      </div>
    </AppShell>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div>
      <p className="text-xl font-semibold text-[#143b5d]">{value}</p>
      <p className="text-xs text-slate-500">{label}</p>
    </div>
  );
}
