"use client";

import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { AppShell } from "../components/AppShell";
import { useAuth } from "../lib/auth";
import type {
  BackgroundResponse,
  IndividualProfileResponse,
  OrganizationProfileResponse,
} from "../lib/api-types";

type UserType = "INDIVIDUAL" | "ORGANIZATION";
type ProfileResponse = IndividualProfileResponse | OrganizationProfileResponse;

type ProfileForm = {
  headline: string;
  description: string;
  location: string;
  website: string;
  profilePicture: string;
  coverImage: string;
  firstName: string;
  lastName: string;
  organizationName: string;
  phone: string;
  industry: string;
  specialties: string;
  cvDocument: string;
};

type BackgroundForm = {
  type: BackgroundResponse["type"];
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  currentlyOngoing: boolean;
};

type ProfileFilter = "All" | "About" | "Background" | "Contact" | "Specialties";

const individualFilters: ProfileFilter[] = ["All", "About", "Background", "Contact"];
const organizationFilters: ProfileFilter[] = ["All", "About", "Contact", "Specialties"];

const emptyForm: ProfileForm = {
  headline: "",
  description: "",
  location: "",
  website: "",
  profilePicture: "",
  coverImage: "",
  firstName: "",
  lastName: "",
  organizationName: "",
  phone: "",
  industry: "",
  specialties: "",
  cvDocument: "",
};

const emptyBackgroundForm: BackgroundForm = {
  type: "EDUCATION",
  title: "",
  description: "",
  startDate: "",
  endDate: "",
  currentlyOngoing: false,
};

export default function ProfilePage() {
  const { appUser, apiFetch, refreshAppUser } = useAuth();
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [form, setForm] = useState<ProfileForm>(emptyForm);
  const [query, setQuery] = useState("");
  const [activeFilter, setActiveFilter] = useState<ProfileFilter>("All");
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savingBackground, setSavingBackground] = useState(false);
  const [editingBackgroundId, setEditingBackgroundId] = useState<number | null>(null);
  const [backgroundForm, setBackgroundForm] = useState<BackgroundForm>(emptyBackgroundForm);
  const [error, setError] = useState("");
  const [savedMessage, setSavedMessage] = useState("");

  const accountType = appUser?.accountType ?? "INDIVIDUAL";
  const isIndividual = accountType === "INDIVIDUAL";
  const filters = isIndividual ? individualFilters : organizationFilters;

  const loadProfile = useCallback(async () => {
    if (!appUser?.profileId || !appUser.accountType) {
      setProfile(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError("");
    setSavedMessage("");

    const endpoint =
      appUser.accountType === "ORGANIZATION"
        ? `/api/organization/${appUser.profileId}?actingUserId=${appUser.id}`
        : `/api/individuals/${appUser.profileId}?actingUserId=${appUser.id}`;

    try {
      const response = await apiFetch(endpoint);
      if (!response.ok) {
        throw new Error(`Could not load profile (${response.status}).`);
      }

      const nextProfile = (await response.json()) as ProfileResponse;
      setProfile(nextProfile);
      setForm(profileToForm(nextProfile, appUser.accountType));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load profile.");
    } finally {
      setLoading(false);
    }
  }, [apiFetch, appUser]);

  useEffect(() => {
    queueMicrotask(() => void loadProfile());
  }, [loadProfile]);

  const displayName = useMemo(() => {
    if (!profile) return appUser?.displayName ?? appUser?.username ?? "Profile";
    if (isIndividual) {
      const individualProfile = profile as IndividualProfileResponse;
      return (
        `${individualProfile.firstName ?? ""} ${individualProfile.lastName ?? ""}`.trim() ||
        appUser?.displayName ||
        appUser?.username ||
        "Individual profile"
      );
    }

    return (
      (profile as OrganizationProfileResponse).organizationName ||
      appUser?.displayName ||
      appUser?.username ||
      "Organization profile"
    );
  }, [appUser?.displayName, appUser?.username, isIndividual, profile]);

  const normalizedQuery = query.trim().toLowerCase();
  const backgrounds = isIndividual ? ((profile as IndividualProfileResponse | null)?.backgrounds ?? []) : [];
  const visibleBackgrounds = backgrounds.filter((item) =>
    !normalizedQuery ||
    [item.title, item.description, item.type].some((value) =>
      (value ?? "").toLowerCase().includes(normalizedQuery),
    ),
  );
  const specialties = !isIndividual
    ? ((profile as OrganizationProfileResponse | null)?.specialties ?? "")
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean)
    : [];
  const visibleSpecialties = specialties.filter(
    (item) => !normalizedQuery || item.toLowerCase().includes(normalizedQuery),
  );

  const updateField = (field: keyof ProfileForm, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const updateBackgroundField = <Field extends keyof BackgroundForm>(
    field: Field,
    value: BackgroundForm[Field],
  ) => {
    setBackgroundForm((current) => ({ ...current, [field]: value }));
  };

  const handleSave = async () => {
    if (!appUser?.profileId || !appUser.accountType) return;

    setSaving(true);
    setError("");
    setSavedMessage("");

    const endpoint =
      appUser.accountType === "ORGANIZATION"
        ? `/api/organization/${appUser.profileId}?actingUserId=${appUser.id}`
        : `/api/individuals/${appUser.profileId}?actingUserId=${appUser.id}`;

    const sharedPayload = {
      headline: optionalValue(form.headline),
      description: optionalValue(form.description),
      location: optionalValue(form.location),
      website: optionalWebsite(form.website),
      profilePicture: optionalValue(form.profilePicture),
      coverImage: optionalValue(form.coverImage),
    };

    const body =
      appUser.accountType === "ORGANIZATION"
        ? {
            ...sharedPayload,
            organizationName: optionalValue(form.organizationName),
            phone: optionalValue(form.phone),
            industry: optionalValue(form.industry),
            specialties: optionalValue(form.specialties),
          }
        : {
            ...sharedPayload,
            firstName: optionalValue(form.firstName),
            lastName: optionalValue(form.lastName),
            phone: optionalValue(form.phone),
            cvDocument: optionalValue(form.cvDocument),
          };

    try {
      const response = await apiFetch(endpoint, {
        method: "PUT",
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        throw new Error(`Could not save profile (${response.status}).`);
      }

      const nextProfile = (await response.json()) as ProfileResponse;
      setProfile(nextProfile);
      setForm(profileToForm(nextProfile, appUser.accountType));
      setEditing(false);
      setSavedMessage("Profile updated.");
      await refreshAppUser();
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "Could not save profile.");
    } finally {
      setSaving(false);
    }
  };

  const resetBackgroundForm = () => {
    setEditingBackgroundId(null);
    setBackgroundForm(emptyBackgroundForm);
  };

  const handleEditBackground = (background: BackgroundResponse) => {
    setEditingBackgroundId(background.backgroundId);
    setBackgroundForm({
      type: background.type,
      title: background.title,
      description: background.description ?? "",
      startDate: background.startDate,
      endDate: background.endDate ?? "",
      currentlyOngoing: Boolean(background.currentlyOngoing),
    });
    setEditing(true);
    setActiveFilter("Background");
    setError("");
    setSavedMessage("");
  };

  const handleSaveBackground = async () => {
    if (!appUser || !isIndividual) return;

    const individualProfileId = appUser.individualProfileId ?? (profile as IndividualProfileResponse | null)?.id;
    if (!individualProfileId) {
      setError("This account does not have an individual profile id yet.");
      return;
    }

    if (!backgroundForm.title.trim() || !backgroundForm.startDate) {
      setError("Background title and start date are required.");
      return;
    }

    setSavingBackground(true);
    setError("");
    setSavedMessage("");

    const endpoint = editingBackgroundId
      ? `/api/individuals/${individualProfileId}/backgrounds/${editingBackgroundId}?actingUserId=${appUser.id}`
      : `/api/individuals/${individualProfileId}/backgrounds?actingUserId=${appUser.id}`;

    try {
      const response = await apiFetch(endpoint, {
        method: editingBackgroundId ? "PUT" : "POST",
        body: JSON.stringify({
          type: backgroundForm.type,
          title: backgroundForm.title.trim(),
          description: optionalValue(backgroundForm.description),
          startDate: backgroundForm.startDate,
          endDate: backgroundForm.currentlyOngoing ? null : optionalValue(backgroundForm.endDate),
          currentlyOngoing: backgroundForm.currentlyOngoing,
        }),
      });

      if (!response.ok) {
        throw new Error(`Could not save background (${response.status}).`);
      }

      resetBackgroundForm();
      setSavedMessage("Background updated.");
      await loadProfile();
    } catch (backgroundError) {
      setError(backgroundError instanceof Error ? backgroundError.message : "Could not save background.");
    } finally {
      setSavingBackground(false);
    }
  };

  const handleDeleteBackground = async (backgroundId: number) => {
    if (!appUser || !isIndividual) return;

    const individualProfileId = appUser.individualProfileId ?? (profile as IndividualProfileResponse | null)?.id;
    if (!individualProfileId) {
      setError("This account does not have an individual profile id yet.");
      return;
    }

    setSavingBackground(true);
    setError("");
    setSavedMessage("");

    try {
      const response = await apiFetch(
        `/api/individuals/${individualProfileId}/backgrounds/${backgroundId}?actingUserId=${appUser.id}`,
        { method: "DELETE" },
      );

      if (!response.ok) {
        throw new Error(`Could not delete background (${response.status}).`);
      }

      resetBackgroundForm();
      setSavedMessage("Background deleted.");
      await loadProfile();
    } catch (backgroundError) {
      setError(backgroundError instanceof Error ? backgroundError.message : "Could not delete background.");
    } finally {
      setSavingBackground(false);
    }
  };

  return (
    <AppShell
      searchValue={query}
      onSearchChange={setQuery}
      searchPlaceholder="Search profile details"
    >
      <div className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
        <aside className="space-y-4 xl:sticky xl:top-24 xl:self-start">
          <section className="overflow-hidden rounded-[1.75rem] border border-white/20 bg-white/85 shadow-2xl backdrop-blur-xl">
            <div
              className="h-24 bg-[#143b5d] bg-cover bg-center"
              style={{
                backgroundImage: `linear-gradient(rgba(20, 59, 93, 0.25), rgba(20, 59, 93, 0.25)), url("${profile?.coverImage || "/blur_cover.png"}")`,
              }}
            />
            <div className="p-4 pt-0">
              <div
                className="-mt-9 h-[72px] w-[72px] rounded-full border-4 border-white bg-white bg-contain bg-center bg-no-repeat shadow-lg"
                style={{ backgroundImage: `url("${profile?.profilePicture || "/logo.png"}")` }}
              />
              <div className="mt-3">
                <h1 className="text-lg font-semibold text-[#143b5d]">{displayName}</h1>
                <p className="mt-1 text-sm text-slate-600">{profile?.headline || "No headline yet"}</p>
                <p className="mt-1 text-xs text-slate-500">{profile?.location || "No location yet"}</p>
              </div>
              <span className="mt-4 inline-flex rounded-full bg-[#143b5d]/10 px-3 py-1 text-xs font-semibold text-[#143b5d]">
                {isIndividual ? "Individual account" : "Organization account"}
              </span>
            </div>
          </section>

          <section className="rounded-[1.75rem] border border-white/20 bg-white/85 p-4 shadow-2xl backdrop-blur-xl">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="text-sm font-semibold text-[#143b5d]">Sections</h2>
                <p className="text-xs text-slate-500">Filter this profile</p>
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
              <Snapshot label="Account" value={isIndividual ? "1" : "Org"} />
              <Snapshot label={isIndividual ? "Background" : "Specialties"} value={isIndividual ? backgrounds.length : specialties.length} />
              <Snapshot label="Contact" value={[profile?.website, profile?.location].filter(Boolean).length} />
            </div>
          </section>
        </aside>

        <section className="min-w-0 rounded-[2rem] border border-white/20 bg-white/90 shadow-2xl backdrop-blur-xl">
          <div className="border-b border-slate-200 px-5 py-4 sm:px-6">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#143b5d]">
              Profile
            </p>
            <div className="mt-2 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <h2 className="text-2xl font-semibold text-slate-900">
                  {isIndividual ? "Individual details" : "Organization details"}
                </h2>
                <p className="mt-1 text-sm text-slate-600">
                  This profile is connected to your account and stored in the backend.
                </p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setEditing((current) => !current);
                  setSavedMessage("");
                  setError("");
                }}
                className="h-10 rounded-xl bg-[#143b5d] px-4 text-xs font-bold uppercase tracking-wide text-white shadow-sm transition hover:bg-[#1d5485]"
              >
                {editing ? "Close editor" : "Edit profile"}
              </button>
            </div>
          </div>

          <div className="space-y-4 p-5 sm:p-6">
            {loading && <EmptyState label="Loading profile..." />}
            {!loading && !profile && (
              <EmptyState label="No profile was found for this account. Sign out and sign in again to let the backend create it." />
            )}
            {error && (
              <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
                {error}
              </div>
            )}
            {savedMessage && (
              <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700">
                {savedMessage}
              </div>
            )}

            {profile && editing && (
              <ProfileSection title="Edit Profile" description="Update the fields that should appear on your public profile.">
                <div className="grid gap-4 lg:grid-cols-2">
                  {isIndividual ? (
                    <>
                      <TextField label="First name" value={form.firstName} onChange={(value) => updateField("firstName", value)} />
                      <TextField label="Last name" value={form.lastName} onChange={(value) => updateField("lastName", value)} />
                    </>
                  ) : (
                    <>
                      <TextField label="Organization name" value={form.organizationName} onChange={(value) => updateField("organizationName", value)} />
                      <TextField label="Industry" value={form.industry} onChange={(value) => updateField("industry", value)} />
                    </>
                  )}
                  <TextField label="Headline" value={form.headline} onChange={(value) => updateField("headline", value)} />
                  <TextField label="Location" value={form.location} onChange={(value) => updateField("location", value)} />
                  <TextField label="Website" value={form.website} onChange={(value) => updateField("website", value)} placeholder="https://example.com" />
                  <TextField label="Phone" value={form.phone} onChange={(value) => updateField("phone", value)} placeholder="+40712345678" />
                  <TextField label="Profile picture URL" value={form.profilePicture} onChange={(value) => updateField("profilePicture", value)} />
                  <TextField label="Cover image URL" value={form.coverImage} onChange={(value) => updateField("coverImage", value)} />
                  {isIndividual ? (
                    <TextField label="CV document URL" value={form.cvDocument} onChange={(value) => updateField("cvDocument", value)} />
                  ) : (
                    <TextField label="Specialties" value={form.specialties} onChange={(value) => updateField("specialties", value)} placeholder="Software, AI, Cloud" />
                  )}
                  <div className="lg:col-span-2">
                    <TextAreaField label="Description" value={form.description} onChange={(value) => updateField("description", value)} />
                  </div>
                </div>

                {isIndividual && (
                  <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                    <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
                      <div>
                        <h4 className="text-sm font-semibold text-[#143b5d]">Background item</h4>
                        <p className="text-xs text-slate-500">Add education, work experience, or projects.</p>
                      </div>
                      {editingBackgroundId && (
                        <button
                          type="button"
                          onClick={resetBackgroundForm}
                          className="rounded-full border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
                        >
                          New item
                        </button>
                      )}
                    </div>
                    <div className="mt-4 grid gap-4 lg:grid-cols-2">
                      <label className="block">
                        <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">Type</span>
                        <select
                          value={backgroundForm.type}
                          onChange={(event) =>
                            updateBackgroundField("type", event.target.value as BackgroundForm["type"])
                          }
                          className="mt-1 h-11 w-full rounded-xl border border-slate-300 bg-white px-3 text-sm text-slate-900 outline-none transition focus:border-[#143b5d] focus:ring-2 focus:ring-[#143b5d]/15"
                        >
                          <option value="EDUCATION">Education</option>
                          <option value="WORK_EXPERIENCE">Work experience</option>
                          <option value="PROJECT">Project</option>
                        </select>
                      </label>
                      <TextField label="Title" value={backgroundForm.title} onChange={(value) => updateBackgroundField("title", value)} />
                      <TextField label="Start date" type="date" value={backgroundForm.startDate} onChange={(value) => updateBackgroundField("startDate", value)} />
                      <TextField
                        label="End date"
                        type="date"
                        value={backgroundForm.endDate}
                        onChange={(value) => updateBackgroundField("endDate", value)}
                        disabled={backgroundForm.currentlyOngoing}
                      />
                      <label className="flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700">
                        <input
                          type="checkbox"
                          checked={backgroundForm.currentlyOngoing}
                          onChange={(event) => updateBackgroundField("currentlyOngoing", event.target.checked)}
                          className="h-4 w-4 accent-[#143b5d]"
                        />
                        Currently ongoing
                      </label>
                      <div className="lg:col-span-2">
                        <TextAreaField
                          label="Background description"
                          value={backgroundForm.description}
                          onChange={(value) => updateBackgroundField("description", value)}
                        />
                      </div>
                    </div>
                    <div className="mt-4 flex justify-end">
                      <button
                        type="button"
                        onClick={handleSaveBackground}
                        disabled={savingBackground}
                        className="h-10 rounded-xl bg-[#143b5d] px-4 text-xs font-bold uppercase tracking-wide text-white shadow-sm transition hover:bg-[#1d5485] disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        {savingBackground ? "Saving..." : editingBackgroundId ? "Update background" : "Add background"}
                      </button>
                    </div>
                  </div>
                )}

                <div className="mt-4 flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => {
                      setForm(profileToForm(profile, accountType));
                      setEditing(false);
                    }}
                    className="h-10 rounded-xl border border-slate-300 bg-white px-4 text-xs font-bold uppercase tracking-wide text-slate-700 transition hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    onClick={handleSave}
                    disabled={saving}
                    className="h-10 rounded-xl bg-[#143b5d] px-4 text-xs font-bold uppercase tracking-wide text-white shadow-sm transition hover:bg-[#1d5485] disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {saving ? "Saving..." : "Save changes"}
                  </button>
                </div>
              </ProfileSection>
            )}

            {profile && (activeFilter === "All" || activeFilter === "About") && (
              <ProfileSection title="About" description={profile.description || "No description yet."}>
                <div className="grid gap-3 sm:grid-cols-3">
                  <DetailCard label="Location" value={profile.location || "Not set"} />
                  <DetailCard label="Website" value={profile.website || "Not set"} />
                  <DetailCard label="Headline" value={profile.headline || "Not set"} />
                </div>
              </ProfileSection>
            )}

            {profile && (activeFilter === "All" || activeFilter === "Contact") && (
              <ProfileSection title="Contact" description="Public contact details for this profile.">
                <div className="grid gap-3 sm:grid-cols-3">
                  <DetailCard label="Email" value={appUser?.email ?? "Not set"} />
                  <DetailCard label="Phone" value={(isIndividual ? (profile as IndividualProfileResponse).phone : (profile as OrganizationProfileResponse).phone) || "Not set"} />
                  <DetailCard label="Account" value={isIndividual ? "Individual" : "Organization"} />
                </div>
              </ProfileSection>
            )}

            {profile && isIndividual && (activeFilter === "All" || activeFilter === "Background") && (
              <ProfileSection title="Background" description="Education, work, and project history.">
                {visibleBackgrounds.length > 0 ? (
                  <div className="space-y-3">
                    {visibleBackgrounds.map((background) => (
                      <BackgroundCard
                        key={background.backgroundId}
                        background={background}
                        editable={editing}
                        saving={savingBackground}
                        onEdit={() => handleEditBackground(background)}
                        onDelete={() => handleDeleteBackground(background.backgroundId)}
                      />
                    ))}
                  </div>
                ) : (
                  <EmptyState label="No background items found for this profile." />
                )}
              </ProfileSection>
            )}

            {profile && !isIndividual && (activeFilter === "All" || activeFilter === "Specialties") && (
              <ProfileSection title="Specialties" description="Areas this organization focuses on.">
                {visibleSpecialties.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {visibleSpecialties.map((specialty) => (
                      <span
                        key={specialty}
                        className="rounded-full bg-[#143b5d]/10 px-3 py-1.5 text-xs font-semibold text-[#143b5d]"
                      >
                        {specialty}
                      </span>
                    ))}
                  </div>
                ) : (
                  <EmptyState label="No specialties have been added yet." />
                )}
              </ProfileSection>
            )}
          </div>
        </section>
      </div>
    </AppShell>
  );
}

function profileToForm(profile: ProfileResponse, accountType: UserType): ProfileForm {
  const base = {
    ...emptyForm,
    headline: profile.headline ?? "",
    description: profile.description ?? "",
    location: profile.location ?? "",
    website: profile.website ?? "",
    profilePicture: profile.profilePicture ?? "",
    coverImage: profile.coverImage ?? "",
  };

  if (accountType === "ORGANIZATION") {
    const organizationProfile = profile as OrganizationProfileResponse;
    return {
      ...base,
      organizationName: organizationProfile.organizationName ?? "",
      phone: organizationProfile.phone ?? "",
      industry: organizationProfile.industry ?? "",
      specialties: organizationProfile.specialties ?? "",
    };
  }

  const individualProfile = profile as IndividualProfileResponse;
  return {
    ...base,
    firstName: individualProfile.firstName ?? "",
    lastName: individualProfile.lastName ?? "",
    phone: individualProfile.phone ?? "",
    cvDocument: individualProfile.cvDocument ?? "",
  };
}

function optionalValue(value: string) {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}

function optionalWebsite(value: string) {
  const trimmed = value.trim();
  if (!trimmed) return null;
  return /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
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
      <div className="mt-1 break-words text-sm font-semibold text-slate-700">{value}</div>
    </div>
  );
}

function Snapshot({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-2xl border border-white/60 bg-white/80 p-3">
      <div className="flex min-h-12 items-center justify-center text-center text-[10px] font-semibold leading-tight text-slate-500">
        {label}
      </div>
      <div className="mt-1 text-lg font-semibold text-[#143b5d]">{value}</div>
    </div>
  );
}

function TextField({
  label,
  value,
  onChange,
  placeholder,
  type = "text",
  disabled = false,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  type?: string;
  disabled?: boolean;
}) {
  return (
    <label className="block">
      <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        disabled={disabled}
        className="mt-1 h-11 w-full rounded-xl border border-slate-300 bg-white px-3 text-sm text-slate-900 outline-none transition focus:border-[#143b5d] focus:ring-2 focus:ring-[#143b5d]/15 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
      />
    </label>
  );
}

function TextAreaField({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className="block">
      <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</span>
      <textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        rows={4}
        className="mt-1 w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-[#143b5d] focus:ring-2 focus:ring-[#143b5d]/15"
      />
    </label>
  );
}

function BackgroundCard({
  background,
  editable = false,
  saving = false,
  onEdit,
  onDelete,
}: {
  background: BackgroundResponse;
  editable?: boolean;
  saving?: boolean;
  onEdit?: () => void;
  onDelete?: () => void;
}) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <span className="rounded-full bg-[#143b5d]/10 px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wide text-[#143b5d]">
              {background.type.replace("_", " ")}
            </span>
            <p className="text-sm font-semibold text-slate-700">{background.title}</p>
          </div>
          {background.description && (
            <p className="mt-1 text-xs text-slate-500">{background.description}</p>
          )}
          <p className="mt-1 text-xs text-slate-500">
            {background.startDate} - {background.currentlyOngoing ? "Present" : background.endDate ?? "Not set"}
          </p>
        </div>
        {editable && (
          <div className="flex shrink-0 gap-2">
            <button
              type="button"
              onClick={onEdit}
              disabled={saving}
              className="rounded-full border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
            >
              Edit
            </button>
            <button
              type="button"
              onClick={onDelete}
              disabled={saving}
              className="rounded-full border border-red-200 bg-red-50 px-3 py-1.5 text-xs font-semibold text-red-700 transition hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-60"
            >
              Delete
            </button>
          </div>
        )}
      </div>
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
