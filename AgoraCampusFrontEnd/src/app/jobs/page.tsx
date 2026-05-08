"use client";

import { FormEvent, useMemo, useState } from "react";
import { AppShell } from "../components/AppShell";

type AccountType = "individual" | "organization";
type OpportunityType = "Volunteer" | "Internship" | "Project" | "Competition";
type OpportunityFilter = "All" | OpportunityType;

type Opportunity = {
  id: string;
  ownerType: AccountType;
  postedByMe: boolean;
  type: OpportunityType;
  title: string;
  organization: string;
  location: string;
  mode: "On-site" | "Hybrid" | "Remote";
  description: string;
  skills: string[];
};

type FormState = {
  title: string;
  organization: string;
  location: string;
  mode: Opportunity["mode"];
  description: string;
  skills: string;
};

const filters: OpportunityFilter[] = ["All", "Volunteer", "Internship", "Project", "Competition"];

const initialOpportunities: Opportunity[] = [
  {
    id: "job-1",
    ownerType: "organization",
    postedByMe: true,
    type: "Volunteer",
    title: "Campus open day volunteer team",
    organization: "Agora Campus",
    location: "Brasov",
    mode: "On-site",
    description: "Help with registration, room guidance, and mentor support during the open day.",
    skills: ["Communication", "Planning"],
  },
  {
    id: "job-2",
    ownerType: "organization",
    postedByMe: false,
    type: "Internship",
    title: "Frontend Internship",
    organization: "Kronsoft",
    location: "Remote",
    mode: "Remote",
    description: "Build product UI with React, TypeScript, and a design system.",
    skills: ["React", "TypeScript", "Tailwind"],
  },
  {
    id: "job-3",
    ownerType: "individual",
    postedByMe: true,
    type: "Project",
    title: "Need two teammates for a campus planner",
    organization: "Ion Popescu",
    location: "Hybrid",
    mode: "Hybrid",
    description: "The first milestone is a responsive event dashboard and submission flow.",
    skills: ["Next.js", "UI", "API"],
  },
  {
    id: "job-4",
    ownerType: "individual",
    postedByMe: false,
    type: "Competition",
    title: "Hackathon team matching",
    organization: "Alex Radu",
    location: "Bucharest",
    mode: "On-site",
    description: "Looking for a frontend developer and a designer for a student hackathon team.",
    skills: ["Frontend", "Pitch", "Design"],
  },
];

const emptyForm: FormState = {
  title: "",
  organization: "",
  location: "",
  mode: "Hybrid",
  description: "",
  skills: "",
};

export default function Jobs() {
  const [accountType, setAccountType] = useState<AccountType>("individual");
  const [opportunities, setOpportunities] = useState<Opportunity[]>(initialOpportunities);
  const [activeFilter, setActiveFilter] = useState<OpportunityFilter>("All");
  const [postedByMeOnly, setPostedByMeOnly] = useState(false);
  const [query, setQuery] = useState("");
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [createType, setCreateType] = useState<OpportunityType>("Project");
  const [form, setForm] = useState<FormState>(emptyForm);

  const normalizedQuery = query.trim().toLowerCase();

  const availableCreateTypes: OpportunityType[] =
    accountType === "organization"
      ? ["Volunteer", "Internship", "Project", "Competition"]
      : ["Project", "Competition"];

  const visibleOpportunities = useMemo(() => {
    return opportunities.filter((opportunity) => {
      const matchesOwner = !postedByMeOnly || opportunity.postedByMe;
      const matchesFilter = activeFilter === "All" || opportunity.type === activeFilter;
      const matchesSearch =
        !normalizedQuery ||
        [
          opportunity.title,
          opportunity.organization,
          opportunity.location,
          opportunity.mode,
          opportunity.type,
          opportunity.description,
          opportunity.skills.join(" "),
        ]
          .join(" ")
          .toLowerCase()
          .includes(normalizedQuery);

      return matchesOwner && matchesFilter && matchesSearch;
    });
  }, [activeFilter, normalizedQuery, opportunities, postedByMeOnly]);

  const updateForm = (field: keyof FormState, value: string) => {
    setForm((currentForm) => ({ ...currentForm, [field]: value }));
  };

  const openCreate = () => {
    setCreateType(
      activeFilter !== "All" && availableCreateTypes.includes(activeFilter as OpportunityType)
        ? (activeFilter as OpportunityType)
        : availableCreateTypes[0],
    );
    setIsCreateOpen(true);
  };

  const handleCreate = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextOpportunity: Opportunity = {
      id: `job-${Date.now()}`,
      ownerType: accountType,
      postedByMe: true,
      type: createType,
      title: form.title.trim(),
      organization:
        form.organization.trim() || (accountType === "organization" ? "Your organization" : "You"),
      location: form.location.trim() || "Flexible",
      mode: form.mode,
      description: form.description.trim(),
      skills: form.skills
        .split(",")
        .map((skill) => skill.trim())
        .filter(Boolean),
    };

    setOpportunities((currentOpportunities) => [nextOpportunity, ...currentOpportunities]);
    setForm(emptyForm);
    setIsCreateOpen(false);
  };

  return (
    <AppShell
      searchValue={query}
      onSearchChange={setQuery}
      searchPlaceholder="Search opportunities, skills, companies"
    >
      <div className="grid gap-6 lg:grid-cols-[300px_minmax(0,1fr)]">
        <aside className="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h1 className="text-lg font-semibold text-[#143b5d]">Jobs</h1>
            <p className="text-sm text-slate-700">Opportunities, projects, internships, and competitions.</p>

            <div className="mt-4 grid grid-cols-2 gap-2">
              {(["individual", "organization"] as AccountType[]).map((type) => (
                <button
                  key={type}
                  type="button"
                  onClick={() => setAccountType(type)}
                  className={[
                    "rounded-full px-3 py-2 text-sm font-semibold capitalize transition",
                    accountType === type
                      ? "bg-[#143b5d] text-white"
                      : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                  ].join(" ")}
                >
                  {type}
                </button>
              ))}
            </div>
          </section>

          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h2 className="text-sm font-semibold text-[#143b5d]">Filters</h2>
            <div className="mt-3 grid grid-cols-2 gap-2">
              {filters.map((filter) => (
                <button
                  key={filter}
                  type="button"
                  onClick={() => setActiveFilter(filter)}
                  className={[
                    "rounded-full px-3 py-2 text-sm font-semibold transition",
                    activeFilter === filter
                      ? "bg-[#143b5d] text-white"
                      : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                  ].join(" ")}
                >
                  {filter}
                </button>
              ))}
            </div>

            <label className="mt-4 flex items-center gap-3 rounded-2xl border border-white/40 bg-white/80 px-3 py-2 text-sm text-slate-700">
              <input
                type="checkbox"
                checked={postedByMeOnly}
                onChange={(event) => setPostedByMeOnly(event.target.checked)}
                className="h-4 w-4 accent-[#143b5d]"
              />
              Posted by me only
            </label>
          </section>

          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h2 className="text-sm font-semibold text-[#143b5d]">Create</h2>
            <p className="mt-1 text-sm text-slate-700">
              {accountType === "organization"
                ? "Publish opportunities for your team."
                : "Find teammates for a project or competition."}
            </p>
            <button
              type="button"
              onClick={openCreate}
              className="mt-4 w-full rounded-full bg-[#143b5d] px-4 py-2.5 text-sm font-semibold text-white hover:bg-[#1d5485]"
            >
              New opportunity
            </button>
          </section>
        </aside>

        <section className="min-w-0 space-y-4">
          <header className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 className="text-lg font-semibold text-[#143b5d]">Opportunity Feed</h2>
                <p className="text-sm text-slate-700">{visibleOpportunities.length} opportunities visible</p>
              </div>
              <span className="rounded-full bg-[#143b5d]/10 px-3 py-1 text-xs font-semibold text-[#143b5d]">
                {activeFilter === "All" ? "All types" : activeFilter}
              </span>
            </div>
          </header>

          <div className="grid gap-4 xl:grid-cols-2">
            {visibleOpportunities.map((opportunity) => (
              <article
                key={opportunity.id}
                className="rounded-3xl border border-white/20 bg-white/80 p-5 shadow-2xl backdrop-blur-xl"
              >
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div className="min-w-0">
                    <div className="flex flex-wrap gap-2">
                      <span className="rounded-full bg-[#143b5d]/10 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-[#143b5d]">
                        {opportunity.type}
                      </span>
                      <span className="rounded-full bg-white/80 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-slate-700">
                        {opportunity.mode}
                      </span>
                      {opportunity.postedByMe && (
                        <span className="rounded-full bg-emerald-100 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-emerald-700">
                          Mine
                        </span>
                      )}
                    </div>
                    <h3 className="mt-3 text-base font-semibold text-[#143b5d]">{opportunity.title}</h3>
                    <p className="text-sm text-slate-700">
                      {opportunity.organization} | {opportunity.location}
                    </p>
                  </div>
                  <button
                    type="button"
                    className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-[#143b5d] hover:bg-[#143b5d]/10"
                  >
                    View
                  </button>
                </div>

                <p className="mt-3 text-sm leading-6 text-slate-800">{opportunity.description}</p>

                <div className="mt-4 flex flex-wrap gap-2">
                  {opportunity.skills.map((skill) => (
                    <span
                      key={skill}
                      className="rounded-full bg-white/80 px-2.5 py-1 text-xs font-medium text-slate-700"
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </article>
            ))}
          </div>

          {visibleOpportunities.length === 0 && (
            <div className="rounded-3xl border border-dashed border-white/40 bg-white/70 px-6 py-12 text-center shadow-2xl backdrop-blur-xl">
              <h2 className="font-semibold text-[#143b5d]">No opportunities found</h2>
              <p className="mt-1 text-sm text-slate-600">Try another filter or search term.</p>
            </div>
          )}
        </section>
      </div>

      {isCreateOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
          <button
            type="button"
            className="absolute inset-0 bg-black/45"
            onClick={() => setIsCreateOpen(false)}
            aria-label="Close create opportunity modal"
          />
          <section className="relative max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-3xl border border-white/20 bg-white/90 p-5 shadow-2xl backdrop-blur-xl">
            <div className="flex items-start justify-between gap-4 border-b border-slate-200 pb-4">
              <div>
                <p className="text-sm font-semibold uppercase tracking-wide text-[#143b5d]">New opportunity</p>
                <h2 className="mt-1 text-2xl font-semibold text-slate-900">
                  {accountType === "organization" ? "Publish an opportunity" : "Find teammates"}
                </h2>
              </div>
              <button
                type="button"
                onClick={() => setIsCreateOpen(false)}
                className="rounded-full bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
              >
                Close
              </button>
            </div>

            <form onSubmit={handleCreate} className="mt-5 space-y-4">
              <div className="rounded-3xl border border-white/30 bg-white/70 p-4">
                <p className="text-sm font-semibold text-slate-700">Type</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {availableCreateTypes.map((option) => (
                    <button
                      key={option}
                      type="button"
                      onClick={() => setCreateType(option)}
                      className={[
                        "rounded-full px-3 py-2 text-sm font-semibold transition",
                        createType === option
                          ? "bg-[#143b5d] text-white"
                          : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                      ].join(" ")}
                    >
                      {option}
                    </button>
                  ))}
                </div>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="space-y-2">
                  <span className="text-sm font-medium text-slate-700">Title</span>
                  <input
                    required
                    value={form.title}
                    onChange={(event) => updateForm("title", event.target.value)}
                    className="h-11 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                  />
                </label>

                <label className="space-y-2">
                  <span className="text-sm font-medium text-slate-700">Organization or owner</span>
                  <input
                    value={form.organization}
                    onChange={(event) => updateForm("organization", event.target.value)}
                    className="h-11 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                  />
                </label>

                <label className="space-y-2">
                  <span className="text-sm font-medium text-slate-700">Location</span>
                  <input
                    value={form.location}
                    onChange={(event) => updateForm("location", event.target.value)}
                    className="h-11 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                  />
                </label>

                <label className="space-y-2">
                  <span className="text-sm font-medium text-slate-700">Mode</span>
                  <select
                    value={form.mode}
                    onChange={(event) => updateForm("mode", event.target.value)}
                    className="h-11 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                  >
                    <option value="On-site">On-site</option>
                    <option value="Hybrid">Hybrid</option>
                    <option value="Remote">Remote</option>
                  </select>
                </label>
              </div>

              <label className="block space-y-2">
                <span className="text-sm font-medium text-slate-700">Skills, comma separated</span>
                <input
                  value={form.skills}
                  onChange={(event) => updateForm("skills", event.target.value)}
                  className="h-11 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                  placeholder="React, Java, Figma"
                />
              </label>

              <label className="block space-y-2">
                <span className="text-sm font-medium text-slate-700">Description</span>
                <textarea
                  required
                  value={form.description}
                  onChange={(event) => updateForm("description", event.target.value)}
                  className="min-h-28 w-full resize-none rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
                />
              </label>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsCreateOpen(false)}
                  className="rounded-full bg-white px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="rounded-full bg-[#143b5d] px-5 py-2.5 text-sm font-semibold text-white hover:bg-[#1d5485]"
                >
                  Create
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </AppShell>
  );
}
