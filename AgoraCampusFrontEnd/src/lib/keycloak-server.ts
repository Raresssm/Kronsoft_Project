import "server-only";
import type { KeycloakTokenResponse } from "./auth-types";

export type { KeycloakTokenResponse };

type KeycloakConfig = {
  baseUrl: string;
  realm: string;
  clientId: string;
  adminUsername: string;
  adminPassword: string;
};

let cachedAdminToken: { token: string; expiresAt: number } | null = null;

function getConfig(): KeycloakConfig {
  const baseUrl =
    process.env.KEYCLOAK_URL?.replace(/\/$/, "") ??
    process.env.NEXT_PUBLIC_KEYCLOAK_URL?.replace(/\/$/, "") ??
    "http://localhost:8090";
  const realm =
    process.env.KEYCLOAK_REALM ?? process.env.NEXT_PUBLIC_KEYCLOAK_REALM ?? "agora-campus";
  const clientId =
    process.env.KEYCLOAK_CLIENT_ID ??
    process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID ??
    "agora-frontend";
  const adminUsername = process.env.KEYCLOAK_ADMIN ?? "admin";
  const adminPassword = process.env.KEYCLOAK_ADMIN_PASSWORD ?? "admin";

  return { baseUrl, realm, clientId, adminUsername, adminPassword };
}

function realmUrl(config: KeycloakConfig, path = ""): string {
  return `${config.baseUrl}/realms/${config.realm}${path}`;
}

function adminUrl(config: KeycloakConfig, path: string): string {
  return `${config.baseUrl}/admin/realms/${config.realm}${path}`;
}

async function parseError(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as { error_description?: string; error?: string };
    return body.error_description ?? body.error ?? response.statusText;
  } catch {
    return response.statusText;
  }
}

export async function loginWithPassword(
  username: string,
  password: string,
): Promise<KeycloakTokenResponse> {
  const config = getConfig();
  const body = new URLSearchParams({
    grant_type: "password",
    client_id: config.clientId,
    username,
    password,
    scope: "openid profile email",
  });

  const response = await fetch(realmUrl(config, "/protocol/openid-connect/token"), {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
    cache: "no-store",
  });

  if (!response.ok) {
    const message = await parseError(response);
    throw new Error(message);
  }

  return response.json() as Promise<KeycloakTokenResponse>;
}

async function getAdminToken(): Promise<string> {
  const config = getConfig();
  const now = Date.now();

  if (cachedAdminToken && cachedAdminToken.expiresAt > now + 10_000) {
    return cachedAdminToken.token;
  }

  const body = new URLSearchParams({
    grant_type: "password",
    client_id: "admin-cli",
    username: config.adminUsername,
    password: config.adminPassword,
  });

  const response = await fetch(
    `${config.baseUrl}/realms/master/protocol/openid-connect/token`,
    {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body,
      cache: "no-store",
    },
  );

  if (!response.ok) {
    throw new Error(`Keycloak admin login failed: ${await parseError(response)}`);
  }

  const tokens = (await response.json()) as KeycloakTokenResponse;
  cachedAdminToken = {
    token: tokens.access_token,
    expiresAt: now + tokens.expires_in * 1000,
  };
  return tokens.access_token;
}

type KeycloakUser = {
  id: string;
  username?: string;
  email?: string;
};

function resolveUserNames(
  email: string,
  firstName?: string,
  lastName?: string,
): { firstName: string; lastName: string } {
  const cap = (value: string) =>
    value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();

  if (firstName?.trim() && lastName?.trim()) {
    return { firstName: firstName.trim(), lastName: lastName.trim() };
  }

  const localPart = email.split("@")[0] ?? "user";
  const parts = localPart.replace(/[._+-]+/g, " ").trim().split(/\s+/).filter(Boolean);

  return {
    firstName: firstName?.trim() || (parts[0] ? cap(parts[0]) : "User"),
    lastName: lastName?.trim() || (parts.length > 1 ? parts.slice(1).map(cap).join(" ") : "Account"),
  };
}

async function findUserByEmail(email: string): Promise<KeycloakUser | null> {
  const config = getConfig();
  const token = await getAdminToken();
  const query = new URLSearchParams({ email, exact: "true" });

  const response = await fetch(`${adminUrl(config, "/users")}?${query}`, {
    headers: { Authorization: `Bearer ${token}` },
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`Keycloak user lookup failed: ${await parseError(response)}`);
  }

  const users = (await response.json()) as KeycloakUser[];
  return users[0] ?? null;
}

export async function registerUser(input: {
  email: string;
  password: string;
  accountType?: string;
  firstName?: string;
  lastName?: string;
}): Promise<void> {
  const config = getConfig();
  const email = input.email.trim().toLowerCase();
  const token = await getAdminToken();
  const { firstName, lastName } = resolveUserNames(email, input.firstName, input.lastName);

  const existing = await findUserByEmail(email);
  if (existing) {
    throw new Error("An account with this email already exists.");
  }

  const payload: Record<string, unknown> = {
    username: email,
    email,
    firstName,
    lastName,
    enabled: true,
    emailVerified: true,
    credentials: [
      {
        type: "password",
        value: input.password,
        temporary: false,
      },
    ],
  };

  if (input.accountType) {
    payload.attributes = { accountType: [input.accountType] };
  }

  const response = await fetch(adminUrl(config, "/users"), {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`Registration failed: ${await parseError(response)}`);
  }
}

export async function sendPasswordResetEmail(
  email: string,
  redirectUri?: string,
): Promise<void> {
  const config = getConfig();
  const normalizedEmail = email.trim().toLowerCase();
  const user = await findUserByEmail(normalizedEmail);

  // Always succeed from the caller's perspective to avoid email enumeration.
  if (!user) {
    return;
  }

  const token = await getAdminToken();
  const params = new URLSearchParams({
    client_id: config.clientId,
    lifespan: "3600",
  });

  if (redirectUri) {
    params.set("redirect_uri", redirectUri);
  }

  const response = await fetch(
    `${adminUrl(config, `/users/${user.id}/execute-actions-email`)}?${params}`,
    {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(["UPDATE_PASSWORD"]),
      cache: "no-store",
    },
  );

  if (!response.ok) {
    throw new Error(`Password reset failed: ${await parseError(response)}`);
  }
}
