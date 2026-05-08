"use client";

import React, { useDeferredValue, useState } from "react";
import Image from "next/image";
import Link from "next/link";

type NetworkTab = "requests" | "connections";
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

const NETWORK_PROFILES: NetworkProfile[] = [
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

const REQUEST_SOURCE_IDS: number[] = [4, 6];
const TAB_ORDER: NetworkTab[] = ["requests", "connections"];
const TAB_LABELS: Record<NetworkTab, string> = {
  requests: "Requests",
  connections: "Connections",
};

const INITIAL_RELATIONSHIPS: Record<number, Relationship> = {};
for (const profile of NETWORK_PROFILES) {
  INITIAL_RELATIONSHIPS[profile.id] = profile.relationship;
}

export default function Network() {
  const [activeTab, setActiveTab] = useState<NetworkTab>("requests");
  const [query, setQuery] = useState("");
  const [relationships, setRelationships] =
    useState<Record<number, Relationship>>(INITIAL_RELATIONSHIPS);
  const [incomingRequestIds, setIncomingRequestIds] = useState<number[]>(REQUEST_SOURCE_IDS);

  const deferredQuery = useDeferredValue(query);
  const normalizedQuery = deferredQuery.trim().toLowerCase();

  const matchesSearch = (profile: NetworkProfile) => {
    if (!normalizedQuery) {
      return true;
    }

    const haystack = [
      profile.name,
      profile.headline,
      profile.company,
      profile.location,
      profile.skills.join(" "),
    ]
      .join(" ")
      .toLowerCase();

    return haystack.includes(normalizedQuery);
  };

  const requestProfiles = NETWORK_PROFILES.filter((profile) => {
    return incomingRequestIds.includes(profile.id) && matchesSearch(profile);
  });

  const connectionProfiles = NETWORK_PROFILES.filter((profile) => {
    return relationships[profile.id] === "connected" && matchesSearch(profile);
  });

  const visibleProfiles = activeTab === "requests" ? requestProfiles : connectionProfiles;

  const totalConnections = NETWORK_PROFILES.filter(
    (profile) => relationships[profile.id] === "connected",
  ).length;
  const totalPending = NETWORK_PROFILES.filter(
    (profile) => relationships[profile.id] === "pending",
  ).length;

  const updateRelationship = (id: number, relationship: Relationship) => {
    setRelationships((prev) => ({
      ...prev,
      [id]: relationship,
    }));
  };

  const handleWithdraw = (id: number) => {
    updateRelationship(id, "none");
  };

  const handleAcceptRequest = (id: number) => {
    updateRelationship(id, "connected");
    setIncomingRequestIds((prev) => prev.filter((requestId) => requestId !== id));
  };

  const handleIgnoreRequest = (id: number) => {
    updateRelationship(id, "none");
    setIncomingRequestIds((prev) => prev.filter((requestId) => requestId !== id));
  };

  return (
    <div className="relative   overflow-hidden h-screen w-full bg-[white] overflow-x-hidden flex flex-col">
      <Image src="/blur_cover.png" alt="profile" fill sizes="100vw" className="object-cover" />

      <div className="relative w-full h-[50px] bg-white/20 border border-white/20 shadow-2xl flex items-center px-4">
        <div className="relative w-[100px] h-full">
          <Image src="/logo.png" alt="Agora" fill sizes="100px" className="object-contain" priority />
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
              <span className="text-xs mt-1 font-semibold">Network</span>
            </div>
          </Link>
          <Link href="/profile">
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

      <div className="w-[1020px] h-[calc(100vh-120px)] min-h-[640px] max-h-[760px] absolute top-[calc(50%+40px)] -translate-y-1/2 left-[7%] rounded-3xl bg-white border border-white/20 shadow-2xl backdrop-blur-xl overflow-hidden">
        <div className="h-full flex">
          <aside className="w-[320px] h-full bg-gradient-to-b from-white/85 to-white/65 border-r border-white/60">
            <div className="p-5 border-b border-gray-200/70">
              <h2 className="text-[#143b5d] text-lg font-semibold tracking-wide">Network</h2>
              <p className="text-xs text-gray-600 mt-1">Manage your professional relationships</p>
            </div>

            <div className="p-4 border-b border-gray-200/70">
              <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search people, skills, company..."
                className="w-full rounded-xl bg-white/70 border border-gray-200 px-3 py-2 text-sm text-gray-700 outline-none"
              />
            </div>

            <div className="px-4 py-3 border-b border-gray-200/70">
              <div className="grid grid-cols-2 gap-2">
                {TAB_ORDER.map((tab) => (
                  <button
                    key={tab}
                    type="button"
                    onClick={() => setActiveTab(tab)}
                    className={`rounded-lg px-2 py-2 text-xs font-semibold transition ${
                      activeTab === tab
                        ? "bg-[#143b5d] text-white"
                        : "bg-white/70 text-[#143b5d] hover:bg-white"
                    }`}
                  >
                    {TAB_LABELS[tab]}
                  </button>
                ))}
              </div>
            </div>

            <div className="p-4 space-y-3">
              <div className="rounded-xl bg-white/75 border border-gray-200/70 px-3 py-2">
                <p className="text-[11px] uppercase tracking-wider text-gray-500">Connections</p>
                <p className="text-lg font-semibold text-[#143b5d]">{totalConnections}</p>
              </div>
              <div className="rounded-xl bg-white/75 border border-gray-200/70 px-3 py-2">
                <p className="text-[11px] uppercase tracking-wider text-gray-500">Pending Sent</p>
                <p className="text-lg font-semibold text-[#143b5d]">{totalPending}</p>
              </div>
              <div className="rounded-xl bg-white/75 border border-gray-200/70 px-3 py-2">
                <p className="text-[11px] uppercase tracking-wider text-gray-500">Incoming Requests</p>
                <p className="text-lg font-semibold text-[#143b5d]">{incomingRequestIds.length}</p>
              </div>
            </div>
          </aside>

          <section className="flex-1 h-full flex flex-col bg-white/45">
            <header className="h-[76px] px-6 border-b border-gray-200/70 flex items-center justify-between">
              <div>
                <h3 className="text-[#143b5d] font-semibold text-lg">{TAB_LABELS[activeTab]}</h3>
                <p className="text-xs text-gray-600">{visibleProfiles.length} profiles visible</p>
              </div>
              <span className="text-xs text-[#143b5d] bg-white/75 border border-white/70 rounded-full px-3 py-1">
                {normalizedQuery ? `Filtered by "${deferredQuery}"` : "All results"}
              </span>
            </header>

            <div className="flex-1 overflow-y-auto p-5 space-y-3">
              {visibleProfiles.length === 0 ? (
                <div className="h-full flex items-center justify-center">
                  <div className="rounded-2xl border border-dashed border-gray-300 bg-white/70 px-8 py-10 text-center">
                    <p className="text-[#143b5d] font-semibold">No profiles found</p>
                    <p className="text-xs text-gray-600 mt-1">
                      Try changing filters or search terms.
                    </p>
                  </div>
                </div>
              ) : (
                visibleProfiles.map((profile) => {
                  const relationship = relationships[profile.id];
                  const isRequest = incomingRequestIds.includes(profile.id);

                  return (
                    <article
                      key={profile.id}
                      className="rounded-2xl border border-white/60 bg-white/80 p-4 shadow-sm"
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex gap-3">
                          <div className="relative w-14 h-14 rounded-full overflow-hidden border border-gray-300/80 shrink-0">
                            <Image
                              src={profile.avatar}
                              alt={profile.name}
                              fill
                              sizes="56px"
                              className="object-cover"
                            />
                          </div>
                          <div>
                            <h4 className="text-[#143b5d] font-semibold">{profile.name}</h4>
                            <p className="text-sm text-gray-700">{profile.headline}</p>
                            <p className="text-xs text-gray-500 mt-0.5">
                              {profile.company} • {profile.location}
                            </p>
                            <p className="text-xs text-gray-500 mt-1">
                              {profile.mutualConnections} mutual connections
                            </p>
                          </div>
                        </div>
                        {relationship === "connected" ? (
                          <span className="text-[11px] font-semibold bg-emerald-100 text-emerald-700 rounded-full px-2 py-1">
                            Connected
                          </span>
                        ) : relationship === "pending" ? (
                          <span className="text-[11px] font-semibold bg-amber-100 text-amber-700 rounded-full px-2 py-1">
                            Pending
                          </span>
                        ) : null}
                      </div>

                      <div className="flex flex-wrap gap-2 mt-3">
                        {profile.skills.map((skill) => (
                          <span
                            key={skill}
                            className="text-[11px] rounded-full bg-[#143b5d]/10 text-[#143b5d] px-2 py-1"
                          >
                            {skill}
                          </span>
                        ))}
                      </div>

                      <div className="flex items-center gap-2 mt-4">
                        {activeTab === "requests" && isRequest ? (
                          <>
                            <button
                              type="button"
                              onClick={() => handleAcceptRequest(profile.id)}
                              className="rounded-xl bg-[#143b5d] text-white text-xs font-semibold px-4 py-2 hover:bg-[#1d5485] transition"
                            >
                              Accept
                            </button>
                            <button
                              type="button"
                              onClick={() => handleIgnoreRequest(profile.id)}
                              className="rounded-xl bg-white border border-gray-300 text-gray-700 text-xs font-semibold px-4 py-2 hover:bg-gray-100 transition"
                            >
                              Ignore
                            </button>
                          </>
                        ) : relationship === "connected" ? (
                          <Link
                            href="/messages"
                            className="rounded-xl bg-[#143b5d] text-white text-xs font-semibold px-4 py-2 hover:bg-[#1d5485] transition"
                          >
                            Message
                          </Link>
                        ) : relationship === "pending" ? (
                          <button
                            type="button"
                            onClick={() => handleWithdraw(profile.id)}
                            className="rounded-xl bg-white border border-gray-300 text-gray-700 text-xs font-semibold px-4 py-2 hover:bg-gray-100 transition"
                          >
                            Withdraw Request
                          </button>
                        ) : (
                          <span className="text-xs text-gray-600">No available action</span>
                        )}
                      </div>
                    </article>
                  );
                })
              )}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
