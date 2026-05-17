import { apiFetch, ApiError } from "./api";
import { getKeycloak } from "./keycloak";

export type AppUser = {
  id: number;
  keycloakId: string;
  email: string;
  username: string;
  createdAt: string;
};

export async function fetchCurrentUser(): Promise<AppUser | null> {
  try {
    return await apiFetch<AppUser>("/api/users/me");
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return null;
    }
    throw error;
  }
}

export async function createCurrentUser(): Promise<AppUser> {
  const token = getKeycloak().tokenParsed;
  const email = typeof token?.email === "string" ? token.email : "";
  const username =
    (typeof token?.preferred_username === "string" && token.preferred_username) ||
    email ||
    "user";

  return apiFetch<AppUser>("/api/users", {
    method: "POST",
    body: JSON.stringify({ email, username }),
  });
}

export async function ensureAppUser(): Promise<AppUser> {
  const existing = await fetchCurrentUser();
  if (existing) {
    return existing;
  }
  return createCurrentUser();
}
