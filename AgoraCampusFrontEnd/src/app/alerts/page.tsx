"use client";

import React from "react";
import Image from "next/image";
import Link from "next/link";

type AccountType = "INDIVIDUAL" | "ORGANIZATION";


type AlertType = "NEW" | "CREATED" | "APPLIED";
type AlertStatus = "UNREAD" | "READ" | "PENDING" | "ACCEPTED" | "REJECTED";

type AlertItem = {
  id: string;
  type: AlertType;
  status: AlertStatus;
  title: string;
  description?: string;
  createdAt: string;
};

const mockUser = {
  id: "user_ion",
  type: "INDIVIDUAL" as const,
  firstName: "Ion",
  lastName: "Popescu",
  headline: "Software Developer",
  location: "Brașov, România",
  avatar: "/logo.png",
};

const mockIndividualAlerts: AlertItem[] = [
  { id: "ia1", type: "NEW", status: "UNREAD", title: "Nou: Internship Frontend", description: "Remote · Deadline: 2026-06-01", createdAt: "2026-05-08" },
  { id: "ia2", type: "APPLIED", status: "PENDING", title: "Ai aplicat la: Voluntariat Agora", description: "În așteptare.", createdAt: "2026-05-07" },
  { id: "ia3", type: "CREATED", status: "READ", title: "Ai creat: Caut membri proiect", description: "Vizualizări: 24 · Aplicanți: 3", createdAt: "2026-05-05" },
];


type ApplicationStatus = "NEW" | "REVIEWING" | "SHORTLISTED" | "INTERVIEW" | "ACCEPTED" | "REJECTED";

type JobPost = {
  id: string;
  orgId: string;
  title: string;
  location: string;
  type: "volunteer" | "internship" | "project" | "competition";
};

type CandidateApplication = {
  id: string;
  jobId: string;
  orgId: string;
  candidateName: string;
  candidateHeadline?: string;
  status: ApplicationStatus;
  appliedAt: string;
  message?: string;
};

const mockCompany = {
  id: "org_agora",
  type: "ORGANIZATION" as const,
  organizationName: "Agora",
  headline: "NGO · Community & Education",
  location: "Brașov, România",
  avatar: "/logo.png",
};

const mockJobPosts: JobPost[] = [
  { id: "j1", orgId: "org_agora", title: "Voluntariat Social Media", location: "Brașov", type: "volunteer" },
  { id: "j2", orgId: "org_agora", title: "Internship Frontend", location: "Remote", type: "internship" },
  { id: "j3", orgId: "org_kronsoft", title: "Internship QA", location: "Brașov", type: "internship" },
];

const mockApplications: CandidateApplication[] = [
  { id: "a1", jobId: "j1", orgId: "org_agora", candidateName: "Ion Popescu", candidateHeadline: "Software Developer", status: "NEW", appliedAt: "2026-05-08", message: "Salut! Mi-ar plăcea să ajut." },
  { id: "a2", jobId: "j1", orgId: "org_agora", candidateName: "Alex Ionescu", candidateHeadline: "Marketing Intern", status: "REVIEWING", appliedAt: "2026-05-07" },
  { id: "a3", jobId: "j2", orgId: "org_agora", candidateName: "Maria Georgescu", candidateHeadline: "Frontend Dev (React)", status: "SHORTLISTED", appliedAt: "2026-05-06" },
  { id: "a4", jobId: "j3", orgId: "org_kronsoft", candidateName: "Someone Else", candidateHeadline: "QA", status: "NEW", appliedAt: "2026-05-08" },
];

/** -------- helpers -------- */
function pill(cls: string) {
  return `text-[11px] font-semibold px-2 py-1 rounded-full ${cls}`;
}

function individualStatusPill(status: AlertStatus) {
  switch (status) {
    case "UNREAD":
      return pill("bg-blue-100 text-blue-700");
    case "READ":
      return pill("bg-slate-100 text-slate-700");
    case "PENDING":
      return pill("bg-amber-100 text-amber-700");
    case "ACCEPTED":
      return pill("bg-green-100 text-green-700");
    case "REJECTED":
      return pill("bg-red-100 text-red-700");
    default:
      return pill("bg-slate-100 text-slate-700");
  }
}

function individualTypePill(type: AlertType) {
  switch (type) {
    case "NEW":
      return pill("bg-indigo-100 text-indigo-700");
    case "CREATED":
      return pill("bg-purple-100 text-purple-700");
    case "APPLIED":
      return pill("bg-teal-100 text-teal-700");
    default:
      return pill("bg-slate-100 text-slate-700");
  }
}

function appStatusPill(status: ApplicationStatus) {
  switch (status) {
    case "NEW":
      return pill("bg-blue-100 text-blue-700");
    case "REVIEWING":
      return pill("bg-amber-100 text-amber-700");
    case "SHORTLISTED":
      return pill("bg-purple-100 text-purple-700");
    case "INTERVIEW":
      return pill("bg-indigo-100 text-indigo-700");
    case "ACCEPTED":
      return pill("bg-green-100 text-green-700");
    case "REJECTED":
      return pill("bg-red-100 text-red-700");
    default:
      return pill("bg-slate-100 text-slate-700");
  }
}

function jobTypePill(t: JobPost["type"]) {
  switch (t) {
    case "volunteer":
      return pill("bg-teal-100 text-teal-700");
    case "internship":
      return pill("bg-indigo-100 text-indigo-700");
    case "project":
      return pill("bg-purple-100 text-purple-700");
    case "competition":
      return pill("bg-amber-100 text-amber-700");
    default:
      return pill("bg-slate-100 text-slate-700");
  }
}

/** -------- main -------- */
export default function Alerts() {
  // AICI combini:
  const [accountType, setAccountType] = React.useState<AccountType>("INDIVIDUAL"); // schimbă după session

  // INDIVIDUAL state
  const [indTab, setIndTab] = React.useState<"ALL" | AlertType>("ALL");
  const [indStatus, setIndStatus] = React.useState<"ALL" | AlertStatus>("ALL");
  const [indQuery, setIndQuery] = React.useState("");

  // ORG state
  const [orgTab, setOrgTab] = React.useState<"ALL" | ApplicationStatus>("ALL");
  const [orgJob, setOrgJob] = React.useState<"ALL" | string>("ALL");
  const [orgQuery, setOrgQuery] = React.useState("");

  // ORG derived
  const myOrgId = mockCompany.id;
  const myPosts = React.useMemo(() => mockJobPosts.filter((p) => p.orgId === myOrgId), [myOrgId]);

  const jobById = React.useMemo(() => {
    const m = new Map<string, JobPost>();
    myPosts.forEach((p) => m.set(p.id, p));
    return m;
  }, [myPosts]);

  const orgApps = React.useMemo(() => {
    const q = orgQuery.trim().toLowerCase();
    let list = mockApplications.filter((a) => a.orgId === myOrgId && jobById.has(a.jobId));

    if (orgTab !== "ALL") list = list.filter((a) => a.status === orgTab);
    if (orgJob !== "ALL") list = list.filter((a) => a.jobId === orgJob);

    if (q) {
      list = list.filter((a) => {
        const job = jobById.get(a.jobId);
        return (
          a.candidateName.toLowerCase().includes(q) ||
          (a.candidateHeadline ?? "").toLowerCase().includes(q) ||
          (a.message ?? "").toLowerCase().includes(q) ||
          (job?.title ?? "").toLowerCase().includes(q)
        );
      });
    }

    return [...list].sort((a, b) => b.appliedAt.localeCompare(a.appliedAt));
  }, [myOrgId, jobById, orgTab, orgJob, orgQuery]);

  // IND derived
  const indAlerts = React.useMemo(() => {
    const q = indQuery.trim().toLowerCase();
    let list = mockIndividualAlerts;

    if (indTab !== "ALL") list = list.filter((a) => a.type === indTab);
    if (indStatus !== "ALL") list = list.filter((a) => a.status === indStatus);

    if (q) {
      list = list.filter((a) => {
        return (
          a.title.toLowerCase().includes(q) ||
          (a.description ?? "").toLowerCase().includes(q)
        );
      });
    }

    return [...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  }, [indTab, indStatus, indQuery]);

  return (
    <div className="relative min-h-screen w-full bg-white overflow-x-hidden">
      <Image src="/blur_cover.png" alt="profile" fill className="object-cover" />

      {/* Navbar (ca la tine) */}
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
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#143b5d" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-5 h-5">
                    <circle cx="11" cy="11" r="7" />
                    <line x1="16.65" y1="16.65" x2="21" y2="21" />
                  </svg>
                </div>
      
                <Link href="/feed">
                  <div className="absolute left-100 top-1/2 cursor-pointer -translate-y-1/2 text-[#143b5d]">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#143b5d" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6 text-blue-500">
                      <path d="M3 10.5L12 3l9 7.5" />
                      <path d="M5 9.5V21h14V9.5" />
                      <path d="M10 21v-6h4v6" />
                    </svg>
                    <span className="text-xs mt-1">Home</span>
                  </div>
                </Link>
      
                <Link href="/messages">
                  <div className="absolute left-120 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#143b5d" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6 text-blue-500">
                      <path d="M21 15a4 4 0 0 1-4 4H8l-5 3 2-4a4 4 0 0 1-2-3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
                    </svg>
                    <span className="text-xs mt-1">Messages</span>
                  </div>
                </Link>
      
                <Link href="/jobs">
                  <div className="absolute left-140 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#143b5d" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6 text-blue-500">
                      <rect x="3" y="7" width="18" height="14" rx="2" ry="2" />
                      <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                      <path d="M3 13h18" />
                    </svg>
                    <span className="text-xs mt-1">Jobs</span>
                  </div>
                </Link>
      
                <Link href="/alerts">
                  <div className="absolute left-160 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#143b5d" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6 text-blue-500">
                      <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 7h18s-3 0-3-7" />
                      <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                    </svg>
                    <span className="text-xs mt-1">Alerts</span>
                  </div>
                </Link>
                      <Link href="/network">
                  <div className="absolute left-180 top-1/2 -translate-y-1/2 cursor-pointer text-[#143b5d]">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#143b5d" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6 text-blue-500">
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
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#143b5d" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
                      <circle cx="12" cy="12" r="10" />
                      <circle cx="12" cy="10" r="3" />
                      <path d="M8 18c0-2.2 1.8-4 4-4s4 1.8 4 4" />
                    </svg>
                    <span className="text-xs mt-1">Profile</span>
                  </div>
                </Link>
              </div>
            </div>

      {/* Page */}
      <main className="relative z-10 mx-auto max-w-6xl px-6 pt-8 pb-10">
        {/* SWITCH (doar pt test) */}
        <div className="mb-4 flex gap-2">
          <button
            type="button"
            onClick={() => setAccountType("INDIVIDUAL")}
            className={`rounded-xl px-4 py-2 text-sm font-semibold transition ${
              accountType === "INDIVIDUAL" ? "bg-[#143b5d] text-white" : "bg-blue-50 text-[#143b5d] hover:bg-blue-100"
            }`}
          >
            Individual
          </button>
          <button
            type="button"
            onClick={() => setAccountType("ORGANIZATION")}
            className={`rounded-xl px-4 py-2 text-sm font-semibold transition ${
              accountType === "ORGANIZATION" ? "bg-[#143b5d] text-white" : "bg-blue-50 text-[#143b5d] hover:bg-blue-100"
            }`}
          >
            Organization
          </button>
        </div>

        {accountType === "INDIVIDUAL" ? (
          /* ================= INDIVIDUAL UI ================= */
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-[320px_1fr]">
            {/* LEFT user */}
            <aside className="space-y-4 h-fit">
              <div className="rounded-3xl bg-white/90 border border-white/20 shadow-2xl backdrop-blur-xl p-5">
                <div className="flex items-center gap-3">
                  <div className="relative h-12 w-12 overflow-hidden rounded-full border border-blue-100 bg-white">
                    <Image src={mockUser.avatar} alt="avatar" fill className="object-cover" />
                  </div>
                  <div>
                    <div className="text-sm font-semibold text-[#143b5d]">
                      {mockUser.firstName} {mockUser.lastName}
                    </div>
                    <div className="text-xs text-slate-500">{mockUser.headline}</div>
                    <div className="text-xs text-slate-400">{mockUser.location}</div>
                  </div>
                </div>
              </div>

              <div className="rounded-3xl bg-white/90 border border-white/20 shadow-2xl backdrop-blur-xl p-5">
                <div className="text-sm font-semibold text-[#143b5d]">Filtre</div>
                <div className="mt-3 space-y-3">
                  <label className="space-y-2 block">
                    <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Caută</span>
                    <input
                      value={indQuery}
                      onChange={(e) => setIndQuery(e.target.value)}
                      placeholder="titlu / descriere..."
                      className="w-full rounded-xl border border-blue-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                    />
                  </label>

                  <label className="space-y-2 block">
                    <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Status</span>
                    <select
                      value={indStatus}
               
                      className="w-full rounded-xl border border-blue-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                    >
                      <option value="ALL">All</option>
                      <option value="UNREAD">Unread</option>
                      <option value="READ">Read</option>
                      <option value="PENDING">Pending</option>
                      <option value="ACCEPTED">Accepted</option>
                      <option value="REJECTED">Rejected</option>
                    </select>
                  </label>

                  <button
                    type="button"
                    onClick={() => {
                      setIndQuery("");
                      setIndStatus("ALL");
                      setIndTab("ALL");
                    }}
                    className="w-full rounded-2xl bg-blue-50 px-4 py-3 text-sm font-semibold text-[#143b5d] hover:bg-blue-100 transition"
                  >
                    Reset
                  </button>
                </div>
              </div>
            </aside>

            {/* RIGHT list */}
            <section className="rounded-3xl bg-white/90 border border-white/20 shadow-2xl backdrop-blur-xl overflow-hidden">
              <div className="p-6 border-b border-blue-100">
                <div className="text-sm font-semibold uppercase tracking-wide text-blue-600">Alerts</div>
                <div className="mt-1 text-2xl font-bold text-[#143b5d]">Notificările tale</div>

                <div className="mt-4 flex flex-wrap gap-2">
                  {[
                    { key: "ALL" as const, label: "All" },
                    { key: "NEW" as const, label: "New" },
                    { key: "CREATED" as const, label: "Created" },
                    { key: "APPLIED" as const, label: "Applied" },
                  ].map((t) => (
                    <button
                      key={t.key}
                      type="button"
                      onClick={() => setIndTab(t.key)}
                      className={[
                        "rounded-xl px-4 py-2 text-sm font-semibold transition",
                        indTab === t.key ? "bg-[#143b5d] text-white" : "bg-blue-50 text-[#143b5d] hover:bg-blue-100",
                      ].join(" ")}
                    >
                      {t.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="p-6 space-y-4 max-h-[calc(100vh-190px)] overflow-y-auto">
                {indAlerts.map((a) => (
                  <article key={a.id} className="rounded-2xl border border-blue-100 bg-white p-5 shadow-sm hover:shadow-md transition">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="flex flex-wrap items-center gap-2">
                          <span className={individualTypePill(a.type)}>{a.type}</span>
                          <span className={individualStatusPill(a.status)}>{a.status}</span>
                          <span className="text-[11px] text-slate-400">{a.createdAt}</span>
                        </div>
                        <h3 className="mt-2 text-base font-bold text-[#143b5d]">{a.title}</h3>
                        {a.description && <p className="mt-1 text-sm text-slate-600">{a.description}</p>}
                      </div>

                      <button
                        type="button"
                        className="rounded-xl border border-blue-100 bg-blue-50 px-3 py-2 text-sm font-semibold text-[#143b5d] hover:bg-blue-100 transition"
                      >
                        Vezi
                      </button>
                    </div>
                  </article>
                ))}

                {indAlerts.length === 0 && (
                  <div className="rounded-2xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-700">
                    Nu există alerte pentru filtrele selectate.
                  </div>
                )}
              </div>
            </section>
          </div>
        ) : (
          /* ================= ORGANIZATION UI ================= */
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-[320px_1fr]">
            {/* LEFT company */}
            <aside className="space-y-4 h-fit">
              <div className="rounded-3xl bg-white/90 border border-white/20 shadow-2xl backdrop-blur-xl p-5">
                <div className="flex items-center gap-3">
                  <div className="relative h-12 w-12 overflow-hidden rounded-full border border-blue-100 bg-white">
                    <Image src={mockCompany.avatar} alt="avatar" fill className="object-cover" />
                  </div>
                  <div>
                    <div className="text-sm font-semibold text-[#143b5d]">{mockCompany.organizationName}</div>
                    <div className="text-xs text-slate-500">{mockCompany.headline}</div>
                    <div className="text-xs text-slate-400">{mockCompany.location}</div>
                  </div>
                </div>
              </div>

              <div className="rounded-3xl bg-white/90 border border-white/20 shadow-2xl backdrop-blur-xl p-5">
                <div className="text-sm font-semibold text-[#143b5d]">Filtre aplicații</div>

                <div className="mt-3 space-y-3">
                  <label className="space-y-2 block">
                    <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Caută</span>
                    <input
                      value={orgQuery}
                      onChange={(e) => setOrgQuery(e.target.value)}
                      placeholder="candidat / job / mesaj..."
                      className="w-full rounded-xl border border-blue-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                    />
                  </label>

                  <label className="space-y-2 block">
                    <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Anunț</span>
                    <select
                      value={orgJob}
                      onChange={(e) => setOrgJob(e.target.value)}
                      className="w-full rounded-xl border border-blue-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                    >
                      <option value="ALL">Toate</option>
                      {myPosts.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.title}
                        </option>
                      ))}
                    </select>
                  </label>

                  <button
                    type="button"
                    onClick={() => {
                      setOrgQuery("");
                      setOrgJob("ALL");
                      setOrgTab("ALL");
                    }}
                    className="w-full rounded-2xl bg-blue-50 px-4 py-3 text-sm font-semibold text-[#143b5d] hover:bg-blue-100 transition"
                  >
                    Reset
                  </button>
                </div>
              </div>
            </aside>

   
            <section className="rounded-3xl bg-white/90 border border-white/20 shadow-2xl backdrop-blur-xl overflow-hidden">
              <div className="p-6 border-b border-blue-100">
                <div className="text-sm font-semibold uppercase tracking-wide text-blue-600">Company Alerts</div>
                <div className="mt-1 text-2xl font-bold text-[#143b5d]">Aplicații candidați</div>

                <div className="mt-4 flex flex-wrap gap-2">
                  {(
                    [
                      { key: "ALL", label: "All" },
                      { key: "NEW", label: "New" },
                      { key: "REVIEWING", label: "Reviewing" },
                      { key: "SHORTLISTED", label: "Shortlisted" },
                      { key: "INTERVIEW", label: "Interview" },
                      { key: "ACCEPTED", label: "Accepted" },
                      { key: "REJECTED", label: "Rejected" },
                    ] as const
                  ).map((t) => (
                    <button
                      key={t.key}
                      type="button"
                      onClick={() => setOrgTab(t.key)}
                      className={[
                        "rounded-xl px-4 py-2 text-sm font-semibold transition",
                        orgTab === (t.key ) ? "bg-[#143b5d] text-white" : "bg-blue-50 text-[#143b5d] hover:bg-blue-100",
                      ].join(" ")}
                    >
                      {t.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="p-6 space-y-4 max-h-[calc(100vh-190px)] overflow-y-auto">
                {orgApps.map((a) => {
                  const job = jobById.get(a.jobId)!;

                  return (
                    <article key={a.id} className="rounded-2xl border border-blue-100 bg-white p-5 shadow-sm hover:shadow-md transition">
                      <div className="flex items-start justify-between gap-4">
                        <div className="min-w-0">
                          <div className="flex flex-wrap items-center gap-2">
                            <span className={jobTypePill(job.type)}>{job.type}</span>
                            <span className={appStatusPill(a.status)}>{a.status}</span>
                            <span className="text-[11px] text-slate-400">{a.appliedAt}</span>
                          </div>

                          <h3 className="mt-2 text-base font-bold text-[#143b5d] truncate">{a.candidateName}</h3>
                          {a.candidateHeadline && <p className="text-sm text-slate-600">{a.candidateHeadline}</p>}

                          <div className="mt-3 rounded-xl bg-blue-50/60 border border-blue-100 p-3">
                            <div className="text-xs font-semibold text-slate-700">Aplicat la</div>
                            <div className="text-sm font-bold text-[#143b5d]">{job.title}</div>
                            <div className="text-xs text-slate-500 mt-1">{job.location}</div>
                          </div>

                          {a.message && <p className="mt-3 text-sm text-slate-700"></p>}
                        </div>

                        <div className="shrink-0 flex flex-col gap-2">
                          <button
                            type="button"
                            className="rounded-xl border border-blue-100 bg-blue-50 px-3 py-2 text-sm font-semibold text-[#143b5d] hover:bg-blue-100 transition"
                          >
                            Vezi profil
                          </button>
                          <button
                            type="button"
                            className="rounded-xl border border-blue-100 bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition"
                          >
                            Schimbă status
                          </button>
                        </div>
                      </div>
                    </article>
                  );
                })}

                {orgApps.length === 0 && (
                  <div className="rounded-2xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-700">
                    Nu există aplicații pentru filtrele selectate.
                  </div>
                )}
              </div>
            </section>
          </div>
        )}
      </main>
    </div>
  );
}