"use client";

import Image from "next/image";
import Link from "next/link";
import { useMemo, useState } from "react";
import { AppShell } from "../components/AppShell";

type NetworkTab = "requests" | "connections" | "discover";
type Relationship = "none" | "pending" | "connected";

type NetworkProfile = {
  id: number;
  name: string;
  headline: string;
  company: string;
  location: string;
  mutualConnections: number;
  skills: string[];
  avatar: string;
  relationship: Relationship;
};

const profiles: NetworkProfile[] = [
  {
    id: 1,
    name: "Andrei Ionescu",
    headline: "Frontend Engineer",
    company: "PixelForge",
    location: "Bucharest",
    mutualConnections: 9,
    skills: ["React", "TypeScript", "Tailwind"],
    avatar: "/agora.jpg",
    relationship: "none",
  },
  {
    id: 2,
    name: "Mara Dobre",
    headline: "Backend Developer",
    company: "CodeWave SRL",
    location: "Cluj-Napoca",
    mutualConnections: 6,
    skills: ["Java", "Spring Boot", "PostgreSQL"],
    avatar: "/agora_campus.jpg",
    relationship: "pending",
  },
  {
    id: 3,
    name: "Radu Matei",
    headline: "Product Designer",
    company: "NovaLab",
    location: "Iasi",
    mutualConnections: 12,
    skills: ["Figma", "UX Research", "Design Systems"],
    avatar: "/image.png",
    relationship: "connected",
  },
  {
    id: 4,
    name: "Elena Popescu",
    headline: "QA Engineer",
    company: "QualityGrid",
    location: "Timisoara",
    mutualConnections: 4,
    skills: ["Playwright", "Cypress", "API Testing"],
    avatar: "/agora.jpg",
    relationship: "none",
  },
  {
    id: 5,
    name: "Victor Stan",
    headline: "DevOps Engineer",
    company: "InfraPoint",
    location: "Bucharest",
    mutualConnections: 3,
    skills: ["Docker", "Kubernetes", "CI/CD"],
    avatar: "/agora_campus.jpg",
    relationship: "connected",
  },
  {
    id: 6,
    name: "Cristina Barbu",
    headline: "Data Analyst",
    company: "Insightly",
    location: "Sibiu",
    mutualConnections: 7,
    skills: ["SQL", "Python", "Power BI"],
    avatar: "/image.png",
    relationship: "none",
  },
];

const requestSourceIds = [4, 6];

const tabLabels: Record<NetworkTab, string> = {
  requests: "Requests",
  connections: "Connections",
  discover: "Discover",
};

const initialRelationships: Record<number, Relationship> = profiles.reduce(
  (result, profile) => ({ ...result, [profile.id]: profile.relationship }),
  {},
);

export default function Network() {
  const [activeTab, setActiveTab] = useState<NetworkTab>("requests");
  const [query, setQuery] = useState("");
  const [relationships, setRelationships] =
    useState<Record<number, Relationship>>(initialRelationships);
  const [incomingRequestIds, setIncomingRequestIds] = useState<number[]>(requestSourceIds);

  const normalizedQuery = query.trim().toLowerCase();

  const visibleProfiles = useMemo(() => {
    return profiles.filter((profile) => {
      const relationship = relationships[profile.id];
      const matchesTab =
        activeTab === "requests"
          ? incomingRequestIds.includes(profile.id)
          : activeTab === "connections"
            ? relationship === "connected"
            : relationship !== "connected";
      const matchesSearch =
        !normalizedQuery ||
        [profile.name, profile.headline, profile.company, profile.location, profile.skills.join(" ")]
          .join(" ")
          .toLowerCase()
          .includes(normalizedQuery);

      return matchesTab && matchesSearch;
    });
  }, [activeTab, incomingRequestIds, normalizedQuery, relationships]);

  const totalConnections = profiles.filter((profile) => relationships[profile.id] === "connected").length;
  const totalPending = profiles.filter((profile) => relationships[profile.id] === "pending").length;

  const updateRelationship = (id: number, relationship: Relationship) => {
    setRelationships((currentRelationships) => ({
      ...currentRelationships,
      [id]: relationship,
    }));
  };

  const acceptRequest = (id: number) => {
    updateRelationship(id, "connected");
    setIncomingRequestIds((currentIds) => currentIds.filter((requestId) => requestId !== id));
  };

  const ignoreRequest = (id: number) => {
    updateRelationship(id, "none");
    setIncomingRequestIds((currentIds) => currentIds.filter((requestId) => requestId !== id));
  };

  return (
    <AppShell
      searchValue={query}
      onSearchChange={setQuery}
      searchPlaceholder="Search people, skills, company"
    >
      <div className="grid gap-5 lg:grid-cols-[300px_minmax(0,1fr)]">
        <aside className="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h1 className="text-lg font-semibold text-[#143b5d]">Network</h1>
            <p className="text-sm text-slate-700">Manage professional relationships.</p>

            <div className="mt-4 grid grid-cols-3 gap-3 text-center">
              <div>
                <p className="text-xl font-semibold text-[#143b5d]">{totalConnections}</p>
                <p className="text-xs text-slate-500">Connected</p>
              </div>
              <div>
                <p className="text-xl font-semibold text-[#143b5d]">{totalPending}</p>
                <p className="text-xs text-slate-500">Pending</p>
              </div>
              <div>
                <p className="text-xl font-semibold text-[#143b5d]">{incomingRequestIds.length}</p>
                <p className="text-xs text-slate-500">Requests</p>
              </div>
            </div>
          </section>

          <section className="rounded-3xl border border-white/20 bg-white/80 p-3 shadow-2xl backdrop-blur-xl">
            <div className="grid grid-cols-3 gap-2">
              {(Object.keys(tabLabels) as NetworkTab[]).map((tab) => (
                <button
                  key={tab}
                  type="button"
                  onClick={() => setActiveTab(tab)}
                  className={[
                    "min-h-11 rounded-full px-2 py-2 text-[11px] font-semibold leading-tight text-center whitespace-normal break-words transition",
                    activeTab === tab
                      ? "bg-[#143b5d] text-white"
                      : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                  ].join(" ")}
                >
                  {tabLabels[tab]}
                </button>
              ))}
            </div>
          </section>
        </aside>

        <section className="min-w-0 rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl sm:p-5">
          <div className="flex flex-col gap-2 border-b border-slate-200 pb-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-lg font-semibold text-[#143b5d]">{tabLabels[activeTab]}</h2>
              <p className="text-sm text-slate-600">{visibleProfiles.length} profiles visible</p>
            </div>
            <span className="rounded-full bg-[#143b5d]/10 px-3 py-1 text-xs font-semibold text-[#143b5d]">
              {normalizedQuery ? `Filtered by "${query}"` : "All results"}
            </span>
          </div>

          <div className="mt-4 grid gap-4 xl:grid-cols-2">
            {visibleProfiles.map((profile) => {
              const relationship = relationships[profile.id];
              const isIncomingRequest = incomingRequestIds.includes(profile.id);

              return (
                <article key={profile.id} className="rounded-3xl border border-slate-200 bg-white p-4 shadow-sm">
                  <div className="flex min-w-0 items-start justify-between gap-3">
                    <div className="flex min-w-0 gap-3">
                      <div className="relative h-14 w-14 shrink-0 overflow-hidden rounded-full border border-slate-200 bg-white">
                        <Image src={profile.avatar} alt={profile.name} fill sizes="56px" className="object-cover" />
                      </div>
                      <div className="min-w-0">
                        <h3 className="truncate font-semibold text-[#143b5d]">{profile.name}</h3>
                        <p className="text-sm text-slate-700">{profile.headline}</p>
                        <p className="text-xs text-slate-500">
                          {profile.company} | {profile.location}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">
                          {profile.mutualConnections} mutual connections
                        </p>
                      </div>
                    </div>
                    {relationship !== "none" && (
                      <span
                        className={[
                          "shrink-0 rounded-full px-2 py-1 text-[11px] font-semibold",
                          relationship === "connected"
                            ? "bg-emerald-100 text-emerald-700"
                            : "bg-amber-100 text-amber-700",
                        ].join(" ")}
                      >
                        {relationship === "connected" ? "Connected" : "Pending"}
                      </span>
                    )}
                  </div>

                  <div className="mt-3 flex flex-wrap gap-2">
                    {profile.skills.map((skill) => (
                      <span
                        key={skill}
                        className="rounded-full bg-[#143b5d]/10 px-2.5 py-1 text-xs font-medium text-[#143b5d]"
                      >
                        {skill}
                      </span>
                    ))}
                  </div>

                  <div className="mt-4 flex flex-wrap gap-2">
                    {activeTab === "requests" && isIncomingRequest ? (
                      <>
                        <button
                          type="button"
                          onClick={() => acceptRequest(profile.id)}
                          className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white hover:bg-[#1d5485]"
                        >
                          Accept
                        </button>
                        <button
                          type="button"
                          onClick={() => ignoreRequest(profile.id)}
                          className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                        >
                          Ignore
                        </button>
                      </>
                    ) : relationship === "connected" ? (
                      <Link
                        href="/messages"
                        className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white hover:bg-[#1d5485]"
                      >
                        Message
                      </Link>
                    ) : relationship === "pending" ? (
                      <button
                        type="button"
                        onClick={() => updateRelationship(profile.id, "none")}
                        className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                      >
                        Withdraw
                      </button>
                    ) : (
                      <button
                        type="button"
                        onClick={() => updateRelationship(profile.id, "pending")}
                        className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white hover:bg-[#1d5485]"
                      >
                        Connect
                      </button>
                    )}
                  </div>
                </article>
              );
            })}
          </div>

          {visibleProfiles.length === 0 && (
            <div className="mt-4 rounded-3xl border border-dashed border-slate-300 bg-white/80 px-6 py-12 text-center">
              <h2 className="font-semibold text-[#143b5d]">No profiles found</h2>
              <p className="mt-1 text-sm text-slate-600">Try a different tab or search term.</p>
            </div>
          )}
        </section>
      </div>
    </AppShell>
  );
}
