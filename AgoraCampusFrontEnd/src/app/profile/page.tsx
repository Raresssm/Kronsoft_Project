"use client";

import Image from "next/image";
import { useState, type ReactNode } from "react";
import { RequireAuth } from "@/components/RequireAuth";
import { useAuth } from "@/contexts/AuthContext";
import { AppShell } from "../components/AppShell";

type UserType = "INDIVIDUAL" | "ORGANIZATION";

type BackgroundItem = {
  type: "EDUCATION" | "WORK_EXPERIENCE" | "PROJECT";
  title: string;
  description?: string | null;
  startDate: string;
  endDate?: string | null;
  currentlyOngoing: boolean;
};

type OrgEvent = {
  id: string;
  title: string;
  date: string;
  location: string;
  description?: string;
  link?: string;
};

type MockUser = {
  type: UserType;
  firstName?: string;
  lastName?: string;
  headline: string;
  description: string;
  location: string;
  website?: string;
  coverImage?: string;
  skills?: string[];
  interests?: string[];
  backgrounds?: BackgroundItem[];
  organizationName?: string;
  industry?: string;
  specialties?: string;
  phone?: string;
  orgEvents?: OrgEvent[];
};

type ProfileFilter =
  | "All"
  | "About"
  | "Background"
  | "Skills"
  | "Interests"
  | "Industry"
  | "Specializations"
  | "Events";

const mockUser: MockUser = {
  type: "INDIVIDUAL",
  firstName: "Ion",
  lastName: "Popescu",
  headline: "Software Developer",
  description: "Pasionat de Java si Spring Boot. Caut oportunitati in domeniul tech.",
  location: "Brasov, Romania",
  website: "www.ionpopescu.com",
  coverImage: "/blur_cover.png",
  organizationName: "Google Romania",
  industry: "Technology",
  specialties: "Software, AI, Cloud",
  phone: "+40712345678",
  skills: ["Java", "Spring Boot", "React", "PostgreSQL", "Docker", "Git"],
  interests: ["Artificial Intelligence", "Open Source", "Web Development", "Cloud Computing"],
  backgrounds: [
    {
      type: "EDUCATION",
      title: "Universitatea Transilvania",
      description: "Facultatea de Informatica",
      startDate: "2018",
      endDate: "2022",
      currentlyOngoing: false,
    },
    {
      type: "WORK_EXPERIENCE",
      title: "Java Developer la Firma X",
      description: "Dezvoltare aplicatii Spring Boot",
      startDate: "2022",
      endDate: null,
      currentlyOngoing: true,
    },
    {
      type: "PROJECT",
      title: "Agora Campus",
      description: "Platforma pentru studenti",
      startDate: "2024",
      endDate: null,
      currentlyOngoing: true,
    },
  ],
  orgEvents: [
    {
      id: "e1",
      title: "Open Day - Internship Program",
      date: "2026-05-20",
      location: "Bucuresti",
      description: "Prezentare program + Q&A cu echipa.",
      link: "https://example.com/event",
    },
    {
      id: "e2",
      title: "Hackathon Partner Session",
      date: "2026-06-02",
      location: "Online",
      description: "Sesiune despre provocarea propusa de organizatie.",
    },
  ],
};

const individualFilters: ProfileFilter[] = ["All", "About", "Background", "Skills", "Interests"];
const organizationFilters: ProfileFilter[] = ["All", "About", "Industry", "Specializations", "Events"];

export default function ProfilePage() {
  const { appUser } = useAuth();
  const [viewMode, setViewMode] = useState<UserType>(mockUser.type);
  const [activeFilter, setActiveFilter] = useState<ProfileFilter>("All");
  const [query, setQuery] = useState("");

  const normalizedQuery = query.trim().toLowerCase();
  const isIndividual = viewMode === "INDIVIDUAL";
  const filters = isIndividual ? individualFilters : organizationFilters;
  const displayName = isIndividual
    ? `${mockUser.firstName ?? ""} ${mockUser.lastName ?? ""}`.trim() || "Profile"
    : mockUser.organizationName ?? "Organization";
  const headline = isIndividual ? mockUser.headline : mockUser.industry ?? mockUser.headline;

  const matchesQuery = (value?: string | null) =>
    !normalizedQuery || (value ?? "").toLowerCase().includes(normalizedQuery);

  const visibleBackgrounds = isIndividual
    ? (mockUser.backgrounds ?? []).filter((item) => {
        const matchesFilter = activeFilter === "All" || activeFilter === "Background";
        const matchesSearch =
          matchesQuery(item.title) ||
          matchesQuery(item.description ?? "") ||
          matchesQuery(item.startDate) ||
          matchesQuery(item.endDate ?? "");

        return matchesFilter && matchesSearch;
      })
    : [];

  const visibleSkills = isIndividual
    ? (mockUser.skills ?? []).filter(
        (skill) => (activeFilter === "All" || activeFilter === "Skills") && matchesQuery(skill),
      )
    : [];

  const visibleInterests = isIndividual
    ? (mockUser.interests ?? []).filter(
        (interest) =>
          (activeFilter === "All" || activeFilter === "Interests") && matchesQuery(interest),
      )
    : [];

  const visibleEvents = !isIndividual
    ? (mockUser.orgEvents ?? []).filter((event) => {
        const matchesFilter = activeFilter === "All" || activeFilter === "Events";
        const matchesSearch =
          matchesQuery(event.title) || matchesQuery(event.location) || matchesQuery(event.description);

        return matchesFilter && matchesSearch;
      })
    : [];

  const showAbout = activeFilter === "All" || activeFilter === "About";

  const summaryStats = isIndividual
    ? [
        { label: "Background", value: mockUser.backgrounds?.length ?? 0 },
        { label: "Skills", value: mockUser.skills?.length ?? 0 },
        { label: "Interests", value: mockUser.interests?.length ?? 0 },
      ]
    : [
        { label: "Events", value: mockUser.orgEvents?.length ?? 0 },
        {
          label: "Specialties",
          value:
            mockUser.specialties
              ?.split(",")
              .map((item) => item.trim())
              .filter(Boolean).length ?? 0,
        },
        { label: "Contact", value: [mockUser.industry, mockUser.phone].filter(Boolean).length },
      ];

  return (
    <RequireAuth>
    <AppShell
      searchValue={query}
      onSearchChange={setQuery}
      searchPlaceholder="Search profile details"
    >
      {appUser && (
        <section className="mb-4 rounded-2xl border border-emerald-200/80 bg-emerald-50/90 px-4 py-3 text-sm text-emerald-950">
          Connected to API as <strong>{appUser.username}</strong> ({appUser.email}) · user id{" "}
          {appUser.id}
        </section>
      )}
      <div className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
        <aside className="space-y-4 xl:sticky xl:top-24 xl:self-start">
          <section className="rounded-[1.75rem] border border-white/20 bg-white/85 p-4 shadow-2xl backdrop-blur-xl">
            <div className="grid grid-cols-2 gap-2">
              {(["INDIVIDUAL", "ORGANIZATION"] as UserType[]).map((mode) => (
                <button
                  key={mode}
                  type="button"
                  onClick={() => {
                    setViewMode(mode);
                    setActiveFilter("All");
                  }}
                  className={[
                    "rounded-full px-3 py-2 text-sm font-semibold transition",
                    viewMode === mode
                      ? "bg-[#143b5d] text-white"
                      : "bg-white/80 text-[#143b5d] hover:bg-white",
                  ].join(" ")}
                >
                  {mode === "INDIVIDUAL" ? "Individual" : "Organization"}
                </button>
              ))}
            </div>
          </section>

          <section className="rounded-[1.75rem] border border-white/20 bg-white/85 p-4 shadow-2xl backdrop-blur-xl">
            <div className="flex items-center gap-3">
              <div className="relative h-14 w-14 shrink-0 overflow-hidden rounded-full border-4 border-white bg-white shadow-lg">
                <Image
                  src="/logo.png"
                  alt="Profile avatar"
                  fill
                  sizes="56px"
                  className="object-contain p-1"
                />
              </div>
              <div className="min-w-0">
                <h1 className="truncate text-lg font-semibold text-[#143b5d]">{displayName}</h1>
                <p className="truncate text-sm text-slate-600">{headline}</p>
                <p className="truncate text-xs text-slate-500">{mockUser.location}</p>
              </div>
            </div>
          </section>

          <section className="rounded-[1.75rem] border border-white/20 bg-white/85 p-4 shadow-2xl backdrop-blur-xl">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="text-sm font-semibold text-[#143b5d]">Filters</h2>
                <p className="text-xs text-slate-500">Refine the visible profile sections</p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setQuery("");
                  setActiveFilter("All");
                }}
                className="rounded-full border border-white/60 bg-white/80 px-3 py-1.5 text-xs font-semibold text-[#143b5d] transition hover:bg-white"
              >
                Reset
              </button>
            </div>

            <div className="mt-4 grid grid-cols-2 gap-2">
              {filters.map((filter) => (
                <button
                  key={filter}
                  type="button"
                  onClick={() => setActiveFilter(filter)}
                  className={[
                    "rounded-full px-3 py-2 text-sm font-semibold transition",
                    activeFilter === filter
                      ? "bg-[#143b5d] text-white"
                      : "bg-white/80 text-[#143b5d] hover:bg-white",
                  ].join(" ")}
                >
                  {filter}
                </button>
              ))}
            </div>
          </section>

          <section className="rounded-[1.75rem] border border-white/20 bg-white/85 p-4 shadow-2xl backdrop-blur-xl">
            <h2 className="text-sm font-semibold text-[#143b5d]">Snapshot</h2>
            <div className="mt-4 grid grid-cols-3 gap-2 text-center">
              {summaryStats.map((item) => (
                <div key={item.label} className="rounded-2xl border border-white/60 bg-white/80 p-3">
                  <div className="flex min-h-12 items-center justify-center text-center text-[10px] font-semibold leading-tight text-slate-500">
                    {item.label}
                  </div>
                  <div className="mt-1 text-lg font-semibold text-[#143b5d]">{item.value}</div>
                </div>
              ))}
            </div>
          </section>
        </aside>

        <section className="min-w-0 rounded-[2rem] border border-white/20 bg-white/90 shadow-2xl backdrop-blur-xl">
          <div className="border-b border-slate-200 px-5 py-4 sm:px-6">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#143b5d]">
              Profile
            </p>
            <div className="mt-2 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <h2 className="text-2xl font-semibold text-slate-900">
                  {isIndividual ? "Individual details" : "Organization details"}
                </h2>
                <p className="mt-1 text-sm text-slate-600">
                  Functional profile sections with no backend connection.
                </p>
              </div>
              <span className="rounded-full bg-[#143b5d]/10 px-3 py-1 text-xs font-semibold text-[#143b5d]">
                {activeFilter === "All" ? "All sections" : activeFilter}
              </span>
            </div>
          </div>

          <div className="space-y-4 p-5 sm:p-6">
            {showAbout && (
              <ProfileSection title="About" description={mockUser.description}>
                <div className="grid gap-3 sm:grid-cols-3">
                  <DetailCard label="Location" value={mockUser.location} />
                  <DetailCard label="Website" value={mockUser.website ?? "—"} />
                  <DetailCard label="Role" value={displayName} />
                </div>
              </ProfileSection>
            )}

            {isIndividual && (activeFilter === "All" || activeFilter === "Background") && (
              <ProfileSection title="Background" description="Education, work, and projects.">
                {visibleBackgrounds.length > 0 ? (
                  <div className="space-y-3">
                    {visibleBackgrounds.map((bg) => (
                      <div
                        key={`${bg.title}-${bg.startDate}`}
                        className="rounded-2xl border border-slate-200 bg-white p-4"
                      >
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="rounded-full bg-[#143b5d]/10 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-[#143b5d]">
                            {bg.type}
                          </span>
                          <p className="text-sm font-semibold text-slate-700">{bg.title}</p>
                        </div>
                        {bg.description && (
                          <p className="mt-1 text-xs text-slate-500">{bg.description}</p>
                        )}
                        <p className="mt-1 text-xs text-slate-500">
                          {bg.startDate} - {bg.currentlyOngoing ? "Present" : bg.endDate ?? "—"}
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <EmptyState label="No background items match this search." />
                )}
              </ProfileSection>
            )}

            {isIndividual && (activeFilter === "All" || activeFilter === "Skills") && (
              <ProfileSection title="Skills" description="Core technical stack and tools.">
                {visibleSkills.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {visibleSkills.map((skill) => (
                      <span
                        key={skill}
                        className="rounded-full bg-[#143b5d]/10 px-3 py-1.5 text-xs font-semibold text-[#143b5d]"
                      >
                        {skill}
                      </span>
                    ))}
                  </div>
                ) : (
                  <EmptyState label="No matching skills." />
                )}
              </ProfileSection>
            )}

            {isIndividual && (activeFilter === "All" || activeFilter === "Interests") && (
              <ProfileSection title="Interests" description="Topics you follow closely.">
                {visibleInterests.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {visibleInterests.map((interest) => (
                      <span
                        key={interest}
                        className="rounded-full bg-blue-50 px-3 py-1.5 text-xs font-semibold text-blue-700"
                      >
                        {interest}
                      </span>
                    ))}
                  </div>
                ) : (
                  <EmptyState label="No matching interests." />
                )}
              </ProfileSection>
            )}

            {!isIndividual && (activeFilter === "All" || activeFilter === "Industry") && (
              <ProfileSection title="Industry" description="Primary business domain.">
                <DetailCard label="Industry" value={mockUser.industry ?? "—"} />
              </ProfileSection>
            )}

            {!isIndividual && (activeFilter === "All" || activeFilter === "Specializations") && (
              <ProfileSection title="Specializations" description="Areas the organization focuses on.">
                <div className="flex flex-wrap gap-2">
                  {(mockUser.specialties ?? "")
                    .split(",")
                    .map((item) => item.trim())
                    .filter(Boolean)
                    .map((specialty) => (
                      <span
                        key={specialty}
                        className="rounded-full bg-[#143b5d]/10 px-3 py-1.5 text-xs font-semibold text-[#143b5d]"
                      >
                        {specialty}
                      </span>
                    ))}
                </div>
              </ProfileSection>
            )}

            {!isIndividual && (activeFilter === "All" || activeFilter === "Events") && (
              <ProfileSection title="Events" description="Organization events and open sessions.">
                {visibleEvents.length > 0 ? (
                  <div className="space-y-3">
                    {visibleEvents.map((event) => (
                      <div key={event.id} className="rounded-2xl border border-slate-200 bg-white p-4">
                        <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                          <div className="min-w-0">
                            <p className="text-sm font-semibold text-slate-700">{event.title}</p>
                            <p className="mt-1 text-xs text-slate-500">
                              {event.date} - {event.location}
                            </p>
                            {event.description && (
                              <p className="mt-2 text-sm text-slate-600">{event.description}</p>
                            )}
                          </div>
                          {event.link ? (
                            <a
                              href={event.link}
                              target="_blank"
                              rel="noreferrer"
                              className="rounded-full border border-[#143b5d] px-3 py-1.5 text-xs font-semibold text-[#143b5d] transition hover:bg-[#143b5d] hover:text-white"
                            >
                              View
                            </a>
                          ) : (
                            <span className="text-xs text-slate-400">—</span>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <EmptyState label="No matching events." />
                )}
              </ProfileSection>
            )}
          </div>
        </section>
      </div>
    </AppShell>
    </RequireAuth>
  );
}

function ProfileSection({
  title,
  description,
  children,
}: {
  title: string;
  description?: string;
  children: ReactNode;
}) {
  return (
    <section className="rounded-[1.5rem] border border-slate-200 bg-white/90 p-4 shadow-sm">
      <div className="flex flex-col gap-1 border-b border-slate-200 pb-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h3 className="text-base font-semibold text-[#143b5d]">{title}</h3>
          {description && <p className="text-xs text-slate-500">{description}</p>}
        </div>
      </div>
      <div className="pt-4">{children}</div>
    </section>
  );
}

function DetailCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4">
      <div className="text-[11px] uppercase tracking-wide text-slate-400">{label}</div>
      <div className="mt-1 text-sm font-semibold text-slate-700">{value}</div>
    </div>
  );
}

function EmptyState({ label }: { label: string }) {
  return (
    <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-4 py-8 text-center text-sm text-slate-600">
      {label}
    </div>
  );
}
