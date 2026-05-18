"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { AppShell } from "../components/AppShell";
import {
  formatRelativeTime,
  OpportunityApplicationResponse,
  OpportunityResponse,
  OpportunityType,
} from "../lib/api-types";
import { useAuth } from "../lib/auth";

type OpportunityFilter = "All" | OpportunityType;

type FormState = {
  title: string;
  location: string;
  period: string;
  description: string;
  skills: string;
};

type TokenClaims = {
  realm_access?: {
    roles?: string[];
  };
};

const filters: OpportunityFilter[] = ["All", "VOLUNTEERING", "INTERNSHIP", "STUDENT_PROJECT", "COMPETITION"];

const labels: Record<OpportunityType, string> = {
  VOLUNTEERING: "Volunteer",
  INTERNSHIP: "Internship",
  STUDENT_PROJECT: "Project",
  COMPETITION: "Competition",
};

const emptyForm: FormState = {
  title: "",
  location: "",
  period: "Flexible",
  description: "",
  skills: "",
};

export default function Jobs() {
  const { appUser, apiFetch, token } = useAuth();
  const [opportunities, setOpportunities] = useState<OpportunityResponse[]>([]);
  const [applicationsByOpportunity, setApplicationsByOpportunity] = useState<Record<number, OpportunityApplicationResponse[]>>({});
  const [myApplications, setMyApplications] = useState<OpportunityApplicationResponse[]>([]);
  const [activeFilter, setActiveFilter] = useState<OpportunityFilter>("All");
  const [postedByMeOnly, setPostedByMeOnly] = useState(false);
  const [query, setQuery] = useState("");
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingOpportunityId, setEditingOpportunityId] = useState<number | null>(null);
  const [createType, setCreateType] = useState<OpportunityType>("STUDENT_PROJECT");
  const [form, setForm] = useState<FormState>(emptyForm);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const isAdmin = useMemo(() => hasAdminRole(token), [token]);

  const loadOpportunities = useCallback(async () => {
    if (!appUser) return;

    setLoading(true);
    setError("");

    try {
      const [response, myApplicationsResponse] = await Promise.all([
        apiFetch(`/api/opportunities?actingUserId=${appUser.id}`),
        apiFetch(`/api/opportunities/applications/users/${appUser.id}?actingUserId=${appUser.id}`),
      ]);
      if (!response.ok) throw new Error(`Could not load opportunities (${response.status}).`);

      const items = (await response.json()) as OpportunityResponse[];
      setOpportunities(items);
      setMyApplications(myApplicationsResponse.ok ? ((await myApplicationsResponse.json()) as OpportunityApplicationResponse[]) : []);

      const ownApplications = await Promise.all(
        items
          .filter((opportunity) => isAdmin || opportunity.postedByUserId === appUser.id)
          .map(async (opportunity) => {
            const applicationsResponse = await apiFetch(
              `/api/opportunities/${opportunity.opportunityId}/applications?actingUserId=${appUser.id}`,
            );
            return [
              opportunity.opportunityId,
              applicationsResponse.ok
                ? ((await applicationsResponse.json()) as OpportunityApplicationResponse[])
                : [],
            ] as const;
          }),
      );
      setApplicationsByOpportunity(Object.fromEntries(ownApplications));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load opportunities.");
    } finally {
      setLoading(false);
    }
  }, [apiFetch, appUser, isAdmin]);

  useEffect(() => {
    queueMicrotask(() => void loadOpportunities());
  }, [loadOpportunities]);

  const availableCreateTypes: OpportunityType[] =
    appUser?.accountType === "ORGANIZATION"
      ? ["VOLUNTEERING", "INTERNSHIP", "STUDENT_PROJECT", "COMPETITION"]
      : ["STUDENT_PROJECT", "COMPETITION"];

  const normalizedQuery = query.trim().toLowerCase();
  const visibleOpportunities = useMemo(() => {
    return opportunities.filter((opportunity) => {
      const matchesOwner = !postedByMeOnly || opportunity.postedByUserId === appUser?.id;
      const matchesFilter = activeFilter === "All" || opportunity.type === activeFilter;
      const matchesSearch =
        !normalizedQuery ||
        [
          opportunity.title,
          opportunity.location,
          opportunity.period,
          opportunity.type,
          opportunity.description,
          opportunity.additionalInfo ?? "",
          opportunity.postingProfile?.displayName ?? "",
        ]
          .join(" ")
          .toLowerCase()
          .includes(normalizedQuery);

      return matchesOwner && matchesFilter && matchesSearch;
    });
  }, [activeFilter, appUser?.id, normalizedQuery, opportunities, postedByMeOnly]);

  const updateForm = (field: keyof FormState, value: string) => {
    setForm((currentForm) => ({ ...currentForm, [field]: value }));
  };

  const openCreate = () => {
    setEditingOpportunityId(null);
    setForm(emptyForm);
    setCreateType(activeFilter !== "All" && availableCreateTypes.includes(activeFilter) ? activeFilter : availableCreateTypes[0]);
    setIsCreateOpen(true);
  };

  const openEdit = (opportunity: OpportunityResponse) => {
    setEditingOpportunityId(opportunity.opportunityId);
    setCreateType(opportunity.type);
    setForm({
      title: opportunity.title,
      location: opportunity.location,
      period: opportunity.period,
      description: opportunity.description,
      skills: opportunity.additionalInfo ?? "",
    });
    setError("");
    setIsCreateOpen(true);
  };

  const closeEditor = () => {
    setIsCreateOpen(false);
    setEditingOpportunityId(null);
    setForm(emptyForm);
  };

  const buildSubtypePayload = (type: OpportunityType, skills: string) => {
    const skillText = skills.trim() || "General collaboration";
    if (type === "VOLUNTEERING") return { volunteering: { cause: skillText, schedule: form.period, benefits: "Community experience" } };
    if (type === "INTERNSHIP") return { internship: { duration: form.period, compensation: "Not specified", requirements: skillText } };
    if (type === "COMPETITION") {
      const deadline = new Date();
      deadline.setMonth(deadline.getMonth() + 1);
      return {
        competition: {
          theme: skillText,
          eligibility: "Agora Campus members",
          prize: "To be announced",
          deadline: deadline.toISOString().slice(0, 10),
        },
      };
    }
    return { studentProject: { projectDomain: skillText, requiredSkills: skillText, teamSize: 3 } };
  };

  const handleSaveOpportunity = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!appUser) return;

    if (editingOpportunityId) {
      const response = await apiFetch(`/api/opportunities/${editingOpportunityId}?actingUserId=${appUser.id}`, {
        method: "PUT",
        body: JSON.stringify({
          title: form.title.trim(),
          location: form.location.trim() || "Flexible",
          period: form.period.trim() || "Flexible",
          description: form.description.trim(),
          additionalInfo: form.skills.trim() || null,
        }),
      });

      if (!response.ok) {
        setError(`Could not update opportunity (${response.status}).`);
        return;
      }

      closeEditor();
      await loadOpportunities();
      return;
    }

    const profilePayload =
      appUser.accountType === "ORGANIZATION"
        ? { organizationProfileId: appUser.organizationProfileId, individualProfileId: null }
        : { organizationProfileId: null, individualProfileId: appUser.individualProfileId };

    const response = await apiFetch(`/api/opportunities?actingUserId=${appUser.id}`, {
      method: "POST",
      body: JSON.stringify({
        postedByUserId: appUser.id,
        ...profilePayload,
        type: createType,
        title: form.title.trim(),
        location: form.location.trim() || "Flexible",
        period: form.period.trim() || "Flexible",
        description: form.description.trim(),
        additionalInfo: form.skills.trim(),
        ...buildSubtypePayload(createType, form.skills),
      }),
    });

    if (!response.ok) {
      setError(`Could not create opportunity (${response.status}).`);
      return;
    }

    setForm(emptyForm);
    setIsCreateOpen(false);
    await loadOpportunities();
  };

  const deleteOpportunity = async (opportunity: OpportunityResponse) => {
    if (!appUser) return;

    if (!window.confirm(`Delete "${opportunity.title}"?`)) return;

    const response = await apiFetch(`/api/opportunities/${opportunity.opportunityId}?actingUserId=${appUser.id}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      setError(`Could not delete opportunity (${response.status}).`);
      return;
    }

    await loadOpportunities();
  };

  const apply = async (opportunity: OpportunityResponse) => {
    if (!appUser || opportunity.postedByUserId === appUser.id) return;

    const response = await apiFetch(`/api/opportunities/${opportunity.opportunityId}/applications?actingUserId=${appUser.id}`, {
      method: "POST",
      body: JSON.stringify({ applicantUserId: appUser.id }),
    });

    if (!response.ok && response.status !== 409) {
      setError(`Could not apply (${response.status}).`);
      return;
    }

    await loadOpportunities();
  };

  const updateApplicationStatus = async (applicationId: number, status: "ACCEPTED" | "REJECTED") => {
    if (!appUser) return;

    const response = await apiFetch(`/api/opportunities/applications/${applicationId}/status?actingUserId=${appUser.id}`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });

    if (!response.ok) {
      setError(`Could not update application (${response.status}).`);
      return;
    }

    await loadOpportunities();
  };

  const deleteApplication = async (applicationId: number) => {
    if (!appUser) return;

    const response = await apiFetch(`/api/opportunities/applications/${applicationId}?actingUserId=${appUser.id}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      setError(`Could not remove application (${response.status}).`);
      return;
    }

    await loadOpportunities();
  };

  const myApplicationFor = (opportunity: OpportunityResponse) =>
    myApplications.find((application) => application.opportunityId === opportunity.opportunityId);

  return (
    <AppShell searchValue={query} onSearchChange={setQuery} searchPlaceholder="Search opportunities, skills, companies">
      <div className="grid gap-6 lg:grid-cols-[300px_minmax(0,1fr)]">
        <aside className="space-y-4 lg:sticky lg:top-24 lg:self-start">
          <section className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <h1 className="text-lg font-semibold text-[#143b5d]">Jobs</h1>
            <p className="text-sm text-slate-700">Opportunities, projects, internships, and competitions.</p>
            <p className="mt-3 rounded-2xl bg-[#143b5d]/10 px-3 py-2 text-xs font-semibold text-[#143b5d]">
              {appUser?.accountType === "ORGANIZATION" ? "Organization account" : "Individual account"}
            </p>
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
                    activeFilter === filter ? "bg-[#143b5d] text-white" : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                  ].join(" ")}
                >
                  {filter === "All" ? "All" : labels[filter]}
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
            <p className="mt-1 text-sm text-slate-700">Publish a campus opportunity.</p>
            <button type="button" onClick={openCreate} className="mt-4 w-full rounded-full bg-[#143b5d] px-4 py-2.5 text-sm font-semibold text-white hover:bg-[#1d5485]">
              New opportunity
            </button>
          </section>
        </aside>

        <section className="min-w-0 space-y-4">
          <header className="rounded-3xl border border-white/20 bg-white/80 p-4 shadow-2xl backdrop-blur-xl">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 className="text-lg font-semibold text-[#143b5d]">Opportunity Feed</h2>
                <p className="text-sm text-slate-700">
                  {loading ? "Loading..." : `${visibleOpportunities.length} opportunities visible`}
                </p>
              </div>
              <span className="rounded-full bg-[#143b5d]/10 px-3 py-1 text-xs font-semibold text-[#143b5d]">
                {activeFilter === "All" ? "All types" : labels[activeFilter]}
              </span>
            </div>
          </header>

          {error && <p className="rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-600">{error}</p>}

          <div className="grid items-start gap-4 xl:grid-cols-2">
            {visibleOpportunities.map((opportunity) => {
              const mine = opportunity.postedByUserId === appUser?.id;
              const canManage = mine || isAdmin;
              const myApplication = myApplicationFor(opportunity);
              const applicationCount = applicationsByOpportunity[opportunity.opportunityId]?.length ?? 0;
              const applications = applicationsByOpportunity[opportunity.opportunityId] ?? [];

              return (
                <article key={opportunity.opportunityId} className="rounded-3xl border border-white/20 bg-white/80 p-5 shadow-2xl backdrop-blur-xl">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div className="min-w-0">
                      <div className="flex flex-wrap gap-2">
                        <span className="rounded-full bg-[#143b5d]/10 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-[#143b5d]">
                          {labels[opportunity.type]}
                        </span>
                        <span className="rounded-full bg-white/80 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-slate-700">
                        {opportunity.period}
                      </span>
                      {mine && <span className="rounded-full bg-emerald-100 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-emerald-700">Mine</span>}
                      {!mine && isAdmin && <span className="rounded-full bg-amber-100 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-amber-800">Admin</span>}
                      {!mine && myApplication && (
                        <StatusPill status={myApplication.status} />
                      )}
                    </div>
                      <h3 className="mt-3 text-base font-semibold text-[#143b5d]">{opportunity.title}</h3>
                      <p className="text-sm text-slate-700">
                        {opportunity.postingProfile?.displayName ?? "Agora Campus"} | {opportunity.location}
                      </p>
                      <p className="text-xs text-slate-500">{formatRelativeTime(opportunity.createdAt)}</p>
                    </div>
                    <div className="flex flex-wrap gap-2 sm:justify-end">
                      {canManage ? (
                        <>
                          <button
                            type="button"
                            onClick={() => openEdit(opportunity)}
                            className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-[#143b5d] hover:bg-[#143b5d]/10"
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            onClick={() => deleteOpportunity(opportunity)}
                            className="rounded-full border border-red-200 bg-red-50 px-4 py-2 text-sm font-semibold text-red-700 hover:bg-red-100"
                          >
                            Delete
                          </button>
                        </>
                      ) : (
                        <button
                          type="button"
                          disabled={Boolean(myApplication)}
                          onClick={() => apply(opportunity)}
                          className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-[#143b5d] hover:bg-[#143b5d]/10 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                          {myApplication ? applicationButtonLabel(myApplication.status) : "Apply"}
                        </button>
                      )}
                    </div>
                  </div>

                  <p className="mt-3 text-sm leading-6 text-slate-800">{opportunity.description}</p>
                  {opportunity.additionalInfo && <p className="mt-3 text-xs text-slate-500">{opportunity.additionalInfo}</p>}

                  {!canManage && myApplication && (
                    <div className="mt-4 rounded-2xl border border-slate-200 bg-white/70 p-4">
                      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                          <p className="text-sm font-semibold text-[#143b5d]">Your application</p>
                          <p className="text-xs text-slate-500">
                            Applied {formatRelativeTime(myApplication.appliedAt)}
                          </p>
                        </div>
                        <StatusPill status={myApplication.status} />
                      </div>
                      <button
                        type="button"
                        onClick={() => deleteApplication(myApplication.applicationId)}
                        className="mt-3 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                      >
                        Withdraw application
                      </button>
                    </div>
                  )}

                  {canManage && (
                    <div className="mt-4 space-y-3 rounded-2xl border border-slate-200 bg-white/70 p-4">
                      <div className="flex items-center justify-between gap-3">
                        <p className="text-sm font-semibold text-[#143b5d]">Applicants</p>
                        <span className="rounded-full bg-[#143b5d]/10 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-[#143b5d]">
                          {applicationCount}
                        </span>
                      </div>

                      {applications.length === 0 ? (
                        <p className="text-sm text-slate-600">No applications yet.</p>
                      ) : (
                        <div className="space-y-2">
                          {applications.map((application) => (
                            <div
                              key={application.applicationId}
                              className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white p-3 sm:flex-row sm:items-center sm:justify-between"
                            >
                              <div>
                                <Link
                                  href={`/profile?userId=${application.applicantUserId}`}
                                  className="font-semibold text-[#143b5d] hover:underline"
                                >
                                  {application.applicantUsername}
                                </Link>
                                <p className="text-xs text-slate-500">{application.status}</p>
                              </div>

                              {application.status === "PENDING" || application.status === "ACCEPTED" ? (
                                <div className="flex flex-wrap gap-2">
                                  {application.status === "PENDING" && (
                                    <button
                                      type="button"
                                      onClick={() => updateApplicationStatus(application.applicationId, "ACCEPTED")}
                                      className="rounded-full bg-[#143b5d] px-4 py-2 text-sm font-semibold text-white hover:bg-[#1d5485]"
                                    >
                                      Accept
                                    </button>
                                  )}
                                  <button
                                    type="button"
                                    onClick={() => updateApplicationStatus(application.applicationId, "REJECTED")}
                                    className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                                  >
                                    Reject
                                  </button>
                                  {application.status === "ACCEPTED" && <StatusPill status={application.status} />}
                                </div>
                              ) : (
                                <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-slate-600">
                                  {application.status}
                                </span>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </article>
              );
            })}
          </div>

          {!loading && visibleOpportunities.length === 0 && (
            <div className="rounded-3xl border border-dashed border-white/40 bg-white/70 px-6 py-12 text-center shadow-2xl backdrop-blur-xl">
              <h2 className="font-semibold text-[#143b5d]">No opportunities found</h2>
              <p className="mt-1 text-sm text-slate-600">Create one or try another filter.</p>
            </div>
          )}
        </section>
      </div>

      {isCreateOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
          <button type="button" className="absolute inset-0 bg-black/45" onClick={closeEditor} aria-label="Close create opportunity modal" />
          <section className="relative max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-3xl border border-white/20 bg-white/90 p-5 shadow-2xl backdrop-blur-xl">
            <div className="flex items-start justify-between gap-4 border-b border-slate-200 pb-4">
              <div>
                <p className="text-sm font-semibold uppercase tracking-wide text-[#143b5d]">
                  {editingOpportunityId ? "Edit opportunity" : "New opportunity"}
                </p>
                <h2 className="mt-1 text-2xl font-semibold text-slate-900">
                  {editingOpportunityId ? "Update opportunity" : "Publish an opportunity"}
                </h2>
              </div>
              <button type="button" onClick={closeEditor} className="rounded-full bg-white px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100">
                Close
              </button>
            </div>

            <form onSubmit={handleSaveOpportunity} className="mt-5 space-y-4">
              <div className="rounded-3xl border border-white/30 bg-white/70 p-4">
                <p className="text-sm font-semibold text-slate-700">Type</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  {availableCreateTypes.map((option) => (
                    <button
                      key={option}
                      type="button"
                      disabled={Boolean(editingOpportunityId)}
                      onClick={() => setCreateType(option)}
                      className={[
                        "rounded-full px-3 py-2 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-60",
                        createType === option ? "bg-[#143b5d] text-white" : "bg-white text-[#143b5d] hover:bg-[#143b5d]/10",
                      ].join(" ")}
                    >
                      {labels[option]}
                    </button>
                  ))}
                </div>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <Input label="Title" required value={form.title} onChange={(value) => updateForm("title", value)} />
                <Input label="Location" value={form.location} onChange={(value) => updateForm("location", value)} />
                <Input label="Period" value={form.period} onChange={(value) => updateForm("period", value)} />
                <Input label="Skills or theme" value={form.skills} onChange={(value) => updateForm("skills", value)} />
              </div>

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
                {!editingOpportunityId && (
                  <button
                    type="button"
                    onClick={() => setForm(emptyForm)}
                    className="rounded-full bg-white px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-100"
                  >
                    Reset
                  </button>
                )}
                <button type="submit" className="rounded-full bg-[#143b5d] px-5 py-2.5 text-sm font-semibold text-white hover:bg-[#1d5485]">
                  {editingOpportunityId ? "Save changes" : "Create"}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </AppShell>
  );
}

function applicationButtonLabel(status: OpportunityApplicationResponse["status"]) {
  if (status === "PENDING") return "Pending";
  if (status === "ACCEPTED") return "Accepted";
  return "Rejected";
}

function hasAdminRole(token: string | undefined) {
  if (!token) return false;

  try {
    const payload = token.split(".")[1];
    if (!payload) return false;

    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "=");
    const claims = JSON.parse(window.atob(padded)) as TokenClaims;

    return (claims.realm_access?.roles ?? []).some((role) => {
      const normalizedRole = role.toUpperCase();
      return normalizedRole === "ADMIN" || normalizedRole === "ROLE_ADMIN";
    });
  } catch {
    return false;
  }
}

function StatusPill({ status }: { status: OpportunityApplicationResponse["status"] }) {
  const classes: Record<OpportunityApplicationResponse["status"], string> = {
    PENDING: "bg-amber-100 text-amber-800",
    ACCEPTED: "bg-emerald-100 text-emerald-700",
    REJECTED: "bg-red-100 text-red-700",
  };

  return (
    <span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide ${classes[status]}`}>
      {status}
    </span>
  );
}

function Input({
  label,
  value,
  onChange,
  required,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
}) {
  return (
    <label className="space-y-2">
      <span className="text-sm font-medium text-slate-700">{label}</span>
      <input
        required={required}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="h-11 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-[#143b5d] focus:ring-4 focus:ring-[#143b5d]/10"
      />
    </label>
  );
}
