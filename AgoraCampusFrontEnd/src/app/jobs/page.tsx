"use client";

import React, { useMemo, useState } from "react";
import Image from "next/image";
import Link from "next/link";

type AccountType = "individual" | "organization";
type PostType = "volunteer" | "internship" | "project" | "competition";

type Post = {
  id: string;
  ownerId: string; // user id sau org id
  ownerType: AccountType;
  type: PostType;
  title: string;
  org: string;
  location: string;
};

const ORG_POST_TYPES: { type: PostType; label: string }[] = [
  { type: "volunteer", label: "Creează Voluntariat" },
  { type: "internship", label: "Creează Internship" },
  { type: "project", label: "Creează Proiect" },
  { type: "competition", label: "Creează Competiție" },
];

const ALL_FEED_FILTERS: { type: PostType | "all"; label: string }[] = [
  { type: "all", label: "Toate" },
  { type: "volunteer", label: "Voluntariat" },
  { type: "internship", label: "Internship" },
  { type: "project", label: "Proiecte" },
  { type: "competition", label: "Competiții" },
];

export default function Jobs() {
  // demo: schimbă aici când conectezi la session
  const myUserId = "user_ion";
  const myOrgId = "org_agora";

  const [accountType, setAccountType] = useState<AccountType>("individual");

  const [isPreferencesOpen, setIsPreferencesOpen] = useState(false);
  const [isPostMenuOpen, setIsPostMenuOpen] = useState(false);

  const [activeFilter, setActiveFilter] = useState<PostType | "all">("all");

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [createType, setCreateType] = useState<PostType | null>(null);

  // Preferences
  const [postedByMe, setPostedByMe] = useState<"yes" | "no">("no"); // DOAR pentru individual

  const [prefLocation, setPrefLocation] = useState("");
  const [prefFrom, setPrefFrom] = useState("");
  const [prefTo, setPrefTo] = useState("");

  // Org preferences (exemplu)
  const [orgLocation, setOrgLocation] = useState("");
  const [orgDomain, setOrgDomain] = useState("");

  // Form
  const [form, setForm] = useState({
    title: "",
    company: "",
    location: "",
    workMode: "On-site",
    jobType: "Full-time",
    salary: "",
    contactEmail: "",
    requirements: "",
    description: "",
    startDate: "",
    endDate: "",
    skills: "",
    memberRoles: "",
    membersCount: "",
    competitionLink: "",
  });

  const posts: Post[] = [
    {
      id: "1",
      ownerId: myOrgId,
      ownerType: "organization",
      type: "volunteer",
      title: "Voluntariat ONG",
      org: "Agora",
      location: "Brașov",
    },
    {
      id: "2",
      ownerId: "org_kronsoft",
      ownerType: "organization",
      type: "internship",
      title: "Frontend Internship",
      org: "Kronsoft",
      location: "Remote",
    },
    {
      id: "3",
      ownerId: myUserId,
      ownerType: "individual",
      type: "project",
      title: "Caut 2 membri pentru proiect",
      org: "Ion (individual)",
      location: "Hybrid",
    },
    {
      id: "4",
      ownerId: "user_alex",
      ownerType: "individual",
      type: "competition",
      title: "Hackathon – caut echipă",
      org: "Alex (individual)",
      location: "București",
    },
  ];

  const postTypes =
    accountType === "organization"
      ? ORG_POST_TYPES
      : ([
          { type: "project", label: "Caut membri (Proiect)" },
          { type: "competition", label: "Caut membri (Competiție)" },
        ] satisfies { type: PostType; label: string }[]);

  const feedFilters = useMemo(() => {
    if (accountType === "organization") return ALL_FEED_FILTERS;

    if (postedByMe === "yes") {
      return ALL_FEED_FILTERS.filter((f) => f.type === "all" || f.type === "project" || f.type === "competition");
    }

    return ALL_FEED_FILTERS;
  }, [accountType, postedByMe]);

  
  const visiblePosts = useMemo(() => {
    if (accountType === "organization") return posts.filter((p) => p.ownerType === "organization" && p.ownerId === myOrgId);

    if (postedByMe === "yes") return posts.filter((p) => p.ownerType === "individual" && p.ownerId === myUserId);

    return posts;
  }, [accountType, postedByMe]);

  const filteredPosts = useMemo(() => {
    const locQ = prefLocation.trim().toLowerCase();
    const from = prefFrom ? new Date(prefFrom) : null;
    const to = prefTo ? new Date(prefTo) : null;

    return visiblePosts.filter((p) => {
      if (activeFilter !== "all" && p.type !== activeFilter) return false;

      if (locQ) {
        const pLoc = (p.location ?? "").toLowerCase();
        if (!pLoc.includes(locQ)) return false;
      }

      // (opțional) păstrat doar ca placeholder; tu n-ai date pe post în mock
      if (from || to) {
        // aici vei compara cu start/end date din post când le adaugi în model
        return true;
      }

      return true;
    });
  }, [visiblePosts, activeFilter, prefLocation, prefFrom, prefTo]);

  const closeCreate = () => {
    setIsCreateOpen(false);
    setCreateType(null);
  };

  const openCreate = (t: PostType) => {
    setIsPostMenuOpen(false);
    setCreateType(t);
    setIsCreateOpen(true);
  };

  return (
    <div className="relative min-h-screen w-full overflow-x-hidden bg-white">
      <Image src="/blur_cover.png" alt="cover" fill className="object-cover" />

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
      <main className="relative z-0 mx-auto max-w-6xl px-6 pt-8 pb-10">
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[320px_1fr]">
          {/* ASIDE */}
          <aside className="h-fit space-y-4">
            {/* PROFIL */}
            <div className="rounded-3xl bg-white/90 backdrop-blur-xl border border-blue-100 shadow-xl p-5">
              <div className="text-sm font-semibold text-[#143b5d]">Utilizator</div>
              <div className="mt-1 text-xs text-slate-500">
                Tip cont: <span className="font-medium">{accountType}</span>
              </div>

              {/* test */}
              <button
                type="button"
                onClick={() => setAccountType((p) => (p === "individual" ? "organization" : "individual"))}
                className="mt-4 w-full rounded-2xl bg-blue-50 px-4 py-3 text-sm font-semibold text-[#143b5d] hover:bg-blue-100 transition"
              >
                Toggle cont (test)
              </button>
            </div>

            {/* PREFERENCES (dropdown) */}
            <div className="rounded-3xl bg-white/90 backdrop-blur-xl border border-blue-100 shadow-xl p-5">
              <button
                type="button"
                onClick={() => setIsPreferencesOpen((v) => !v)}
                className="w-full flex items-center justify-between gap-3"
              >
                <div className="text-left">
                  <div className="text-sm font-semibold text-[#143b5d]">Preferințe</div>
                  <div className="mt-1 text-xs text-slate-500">
                    {accountType === "individual" ? "Locație, perioadă + Posted by me." : "Setări organizație."}
                  </div>
                </div>

                <div
                  className={[
                    "shrink-0 rounded-xl border border-blue-100 bg-blue-50 px-3 py-2 text-xs font-semibold text-[#143b5d] transition",
                    isPreferencesOpen ? "opacity-100" : "opacity-90",
                  ].join(" ")}
                >
                  {isPreferencesOpen ? "Închide" : "Deschide"}
                </div>
              </button>

              {isPreferencesOpen && (
                <div className="mt-4 space-y-4">
                  {accountType === "individual" ? (
                    <>
                      {/* Posted by me */}
                      <label className="space-y-2 block">
                        <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Posted by me</span>
                        <select
                          value={postedByMe}
                          onChange={(e) => {
                            const v = e.target.value === "yes" ? "yes" : "no";
                            setPostedByMe(v);
                            // dacă user trece pe "yes" și era pe internship/volunteer, îl mutăm pe all
                            if (v === "yes" && (activeFilter === "internship" || activeFilter === "volunteer")) {
                              setActiveFilter("all");
                            }
                          }}
                          className="w-full rounded-xl border border-blue-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        >
                          <option value="yes">Yes</option>
                          <option value="no">No</option>
                        </select>
                        <p className="text-xs text-slate-500">
                          {postedByMe === "yes"
                            ? "Vezi doar anunțurile tale (Project / Competition)."
                            : "Vezi toate tipurile (Volunteer, Internship, Project, Competition)."}
                        </p>
                      </label>

                      {/* Locație */}
                      <label className="space-y-2 block">
                        <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Locație</span>
                        <input
                          value={prefLocation}
                          onChange={(e) => setPrefLocation(e.target.value)}
                          placeholder="Ex: Brașov / Remote"
                          className="w-full rounded-xl border border-blue-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        />
                      </label>

                      {/* Perioadă */}
                      <div className="space-y-2">
                        <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Perioadă</span>
                        <div className="grid grid-cols-2 gap-2">
                          <input
                            type="date"
                            value={prefFrom}
                            onChange={(e) => setPrefFrom(e.target.value)}
                            className="w-full rounded-xl border border-blue-100 bg-white px-3 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                          />
                          <input
                            type="date"
                            value={prefTo}
                            onChange={(e) => setPrefTo(e.target.value)}
                            className="w-full rounded-xl border border-blue-100 bg-white px-3 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                          />
                        </div>
                      </div>

                      <button
                        type="button"
                        onClick={() => {
                          setPostedByMe("no");
                          setPrefLocation("");
                          setPrefFrom("");
                          setPrefTo("");
                          setActiveFilter("all");
                        }}
                        className="w-full rounded-2xl bg-blue-50 px-4 py-3 text-sm font-semibold text-[#143b5d] hover:bg-blue-100 transition"
                      >
                        Resetează preferințele
                      </button>
                    </>
                  ) : (
                    <>
                      <label className="space-y-2 block">
                        <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Locația organizației</span>
                        <input
                          value={orgLocation}
                          onChange={(e) => setOrgLocation(e.target.value)}
                          placeholder="Ex: Brașov"
                          className="w-full rounded-xl border border-blue-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        />
                      </label>

                      <label className="space-y-2 block">
                        <span className="text-xs font-semibold uppercase tracking-wide text-slate-600">Domeniu</span>
                        <input
                          value={orgDomain}
                          onChange={(e) => setOrgDomain(e.target.value)}
                          placeholder="Ex: Educație / IT / ONG"
                          className="w-full rounded-xl border border-blue-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        />
                      </label>

                      <button
                        type="button"
                        onClick={() => {
                          setOrgLocation("");
                          setOrgDomain("");
                        }}
                        className="w-full rounded-2xl bg-blue-50 px-4 py-3 text-sm font-semibold text-[#143b5d] hover:bg-blue-100 transition"
                      >
                        Resetează setările
                      </button>
                    </>
                  )}
                </div>
              )}
            </div>

            {/* POSTARE */}
            <div className="rounded-3xl bg-white/90 backdrop-blur-xl border border-blue-100 shadow-xl p-5">
              <div className="text-sm font-semibold text-[#143b5d]">Anunțuri</div>
              <div className="mt-1 text-xs text-slate-500">
                {accountType === "organization" ? "Creează anunțuri pentru organizația ta." : "Caută membri (Project/Competition)."}
              </div>

              <div className="mt-4 relative">
                <button
                  type="button"
                  onClick={() => setIsPostMenuOpen((v) => !v)}
                  className="w-full rounded-2xl bg-[#143b5d] px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-900/20 hover:bg-[#1d5485] active:scale-[0.99] transition"
                >
                  Postează un anunț
                </button>

                {isPostMenuOpen && (
                  <div className="mt-3 rounded-2xl border border-blue-100 bg-white shadow-xl overflow-hidden">
                    {postTypes.map((pt) => (
                      <button
                        key={pt.type}
                        type="button"
                        onClick={() => openCreate(pt.type)}
                        className="w-full text-left px-4 py-3 text-sm text-slate-700 hover:bg-blue-50 transition"
                      >
                        {pt.label}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </aside>

          {/* FEED */}
          <section className="rounded-3xl bg-white/90 backdrop-blur-xl border border-blue-100 shadow-xl p-6">
            <div className="flex flex-wrap items-center gap-2 border-b border-blue-100 pb-4">
              {feedFilters.map((f) => (
                <button
                  key={f.type}
                  type="button"
                  onClick={() => setActiveFilter(f.type)}
                  className={[
                    "rounded-xl px-4 py-2 text-sm font-semibold transition",
                    activeFilter === f.type ? "bg-[#143b5d] text-white" : "bg-blue-50 text-[#143b5d] hover:bg-blue-100",
                  ].join(" ")}
                >
                  {f.label}
                </button>
              ))}
            </div>

            <div className="mt-5 space-y-4">
              {filteredPosts.map((p) => (
                <article key={p.id} className="rounded-2xl border border-blue-100 bg-white p-5 shadow-sm hover:shadow-md transition">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="text-base font-bold text-[#143b5d]">{p.title}</h3>
                      <p className="mt-1 text-sm text-slate-600">
                        {p.org} · {p.location}
                      </p>
                      <p className="mt-2 text-xs font-semibold uppercase tracking-wide text-blue-600">{p.type}</p>
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

              {filteredPosts.length === 0 && (
                <div className="rounded-2xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-700">
                  Nu există anunțuri pentru filtrele selectate.
                </div>
              )}
            </div>
          </section>
        </div>
      </main>

      {/* MODAL */}
      {isCreateOpen && createType && (
        <div className="fixed inset-0 z-[9999]">
          <button type="button" className="absolute inset-0 bg-black/40" onClick={closeCreate} aria-label="Închide" />
          <div className="absolute inset-0 flex items-center justify-center p-4">
            <div className="w-full max-w-2xl max-h-[85vh] overflow-y-auto rounded-3xl bg-white p-6 shadow-2xl">
              <div className="flex items-start justify-between gap-4 border-b border-blue-100 pb-4">
                <div>
                  <div className="text-sm font-semibold uppercase tracking-wide text-blue-600">Formular</div>
                  <h2 className="mt-1 text-2xl font-bold text-[#143b5d]">
                    {accountType === "individual"
                      ? createType === "project"
                        ? "Caut membri (Proiect)"
                        : "Caut membri (Competiție)"
                      : `Creează ${createType}`}
                  </h2>
                </div>

                <button
                  type="button"
                  onClick={closeCreate}
                  className="rounded-xl bg-blue-50 px-3 py-2 text-sm font-semibold text-[#143b5d] hover:bg-blue-100"
                >
                  Închide
                </button>
              </div>

              <form
                className="mt-5 space-y-4"
                onSubmit={(e) => {
                  e.preventDefault();
                  console.log({ accountType, type: createType, ...form });
                  closeCreate();
                }}
              >
                <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                  <label className="space-y-2">
                    <span className="text-sm font-medium text-slate-700">Titlu</span>
                    <input
                      value={form.title}
                      onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
                      required
                      className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                      placeholder="Titlu anunț"
                    />
                  </label>

                  <label className="space-y-2">
                    <span className="text-sm font-medium text-slate-700">Locație</span>
                    <input
                      value={form.location}
                      onChange={(e) => setForm((p) => ({ ...p, location: e.target.value }))}
                      required
                      className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                      placeholder="Ex: Brașov / Remote"
                    />
                  </label>
                </div>

                <label className="space-y-2 block">
                  <span className="text-sm font-medium text-slate-700">Skill-uri (virgulă)</span>
                  <input
                    value={form.skills}
                    onChange={(e) => setForm((p) => ({ ...p, skills: e.target.value }))}
                    className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                    placeholder="React, TypeScript, Figma"
                  />
                </label>

                {accountType === "individual" && createType === "project" && (
                  <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <label className="space-y-2">
                      <span className="text-sm font-medium text-slate-700">Roluri căutate</span>
                      <input
                        value={form.memberRoles}
                        onChange={(e) => setForm((p) => ({ ...p, memberRoles: e.target.value }))}
                        className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        placeholder="Ex: Backend, UI/UX"
                      />
                    </label>
                    <label className="space-y-2">
                      <span className="text-sm font-medium text-slate-700">Număr membri</span>
                      <input
                        type="number"
                        value={form.membersCount}
                        onChange={(e) => setForm((p) => ({ ...p, membersCount: e.target.value }))}
                        className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        placeholder="Ex: 2"
                      />
                    </label>
                  </div>
                )}

                {accountType === "individual" && createType === "competition" && (
                  <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <label className="space-y-2">
                      <span className="text-sm font-medium text-slate-700">Competiție (link)</span>
                      <input
                        value={form.competitionLink}
                        onChange={(e) => setForm((p) => ({ ...p, competitionLink: e.target.value }))}
                        className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        placeholder="Ex: https://..."
                      />
                    </label>
                    <label className="space-y-2">
                      <span className="text-sm font-medium text-slate-700">Roluri în echipă</span>
                      <input
                        value={form.memberRoles}
                        onChange={(e) => setForm((p) => ({ ...p, memberRoles: e.target.value }))}
                        className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        placeholder="Ex: Frontend, Pitch, Design"
                      />
                    </label>
                  </div>
                )}

                {accountType === "organization" && createType === "internship" && (
                  <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                    <label className="space-y-2">
                      <span className="text-sm font-medium text-slate-700">Companie</span>
                      <input
                        value={form.company}
                        onChange={(e) => setForm((p) => ({ ...p, company: e.target.value }))}
                        className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                        placeholder="Ex: Agora"
                      />
                    </label>

                    <label className="space-y-2">
                      <span className="text-sm font-medium text-slate-700">Tip job</span>
                      <select
                        value={form.jobType}
                        onChange={(e) => setForm((p) => ({ ...p, jobType: e.target.value }))}
                        className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                      >
                        <option value="Full-time">Full-time</option>
                        <option value="Part-time">Part-time</option>
                        <option value="Internship">Internship</option>
                        <option value="Contract">Contract</option>
                      </select>
                    </label>

                    <label className="space-y-2">
                      <span className="text-sm font-medium text-slate-700">Mod lucru</span>
                      <select
                        value={form.workMode}
                        onChange={(e) => setForm((p) => ({ ...p, workMode: e.target.value }))}
                        className="w-full rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                      >
                        <option value="On-site">On-site</option>
                        <option value="Remote">Remote</option>
                        <option value="Hybrid">Hybrid</option>
                      </select>
                    </label>
                  </div>
                )}

                <label className="space-y-2 block">
                  <span className="text-sm font-medium text-slate-700">Descriere</span>
                  <textarea
                    value={form.description}
                    onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
                    required
                    className="min-h-[120px] w-full resize-none rounded-xl border border-blue-100 px-4 py-3 text-sm outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                    placeholder="Descrie anunțul."
                  />
                </label>

                <div className="flex justify-end gap-2 pt-2">
                  <button
                    type="button"
                    onClick={closeCreate}
                    className="rounded-xl bg-blue-50 px-5 py-3 text-sm font-semibold text-[#143b5d] hover:bg-blue-100"
                  >
                    Anulează
                  </button>
                  <button
                    type="submit"
                    className="rounded-xl bg-[#143b5d] px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-900/20 hover:bg-[#1d5485] active:scale-[0.99] transition"
                  >
                    Creează
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}