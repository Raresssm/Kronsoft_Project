"use client";

import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { apiBaseUrl, keycloakConfig } from "./auth-config";

export type AppUser = {
  id: number;
  keycloakId: string;
  email: string;
  username: string;
  createdAt: string;
  accountType: "INDIVIDUAL" | "ORGANIZATION" | null;
  profileId: number | null;
  individualProfileId: number | null;
  organizationProfileId: number | null;
  displayName: string | null;
};

type TokenClaims = {
  email?: string;
  preferred_username?: string;
  username?: string;
  exp?: number;
};

type AuthSession = {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
  claims: TokenClaims;
};

type TokenResponse = {
  access_token: string;
  refresh_token: string;
  expires_in: number;
};

type AuthContextValue = {
  initialized: boolean;
  authenticated: boolean;
  appUser: AppUser | null;
  token: string | undefined;
  error: string;
  login: (username: string, password: string, accountType?: "INDIVIDUAL" | "ORGANIZATION") => Promise<AppUser>;
  register: (email: string, password: string, accountType: "INDIVIDUAL" | "ORGANIZATION") => Promise<AppUser>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<string>;
  apiFetch: (path: string, init?: RequestInit) => Promise<Response>;
  ensureAppUser: () => Promise<AppUser | null>;
  refreshAppUser: () => Promise<AppUser | null>;
};

const AuthContext = createContext<AuthContextValue | null>(null);
const sessionStorageKey = "agora-campus-auth";
const accountTypeStorageKey = "agora-campus-account-type";

function tokenUrl() {
  return `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token`;
}

function logoutUrl() {
  return `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/logout`;
}

function decodeJwt(token: string): TokenClaims {
  const payload = token.split(".")[1];
  if (!payload) {
    return {};
  }

  const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "=");
  return JSON.parse(window.atob(padded)) as TokenClaims;
}

function createSession(tokenResponse: TokenResponse): AuthSession {
  return {
    accessToken: tokenResponse.access_token,
    refreshToken: tokenResponse.refresh_token,
    expiresAt: Date.now() + tokenResponse.expires_in * 1000,
    claims: decodeJwt(tokenResponse.access_token),
  };
}

function readStoredSession() {
  try {
    const value = window.sessionStorage.getItem(sessionStorageKey);
    if (!value) {
      return null;
    }

    return JSON.parse(value) as AuthSession;
  } catch {
    return null;
  }
}

function storeSession(session: AuthSession | null) {
  if (session) {
    window.sessionStorage.setItem(sessionStorageKey, JSON.stringify(session));
  } else {
    window.sessionStorage.removeItem(sessionStorageKey);
  }
}

async function requestToken(body: URLSearchParams) {
  const response = await fetch(tokenUrl(), {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body,
  });

  if (!response.ok) {
    throw new Error(response.status === 401 ? "Invalid email or password." : `Login failed (${response.status}).`);
  }

  return (await response.json()) as TokenResponse;
}

async function readErrorMessage(response: Response) {
  try {
    const body = (await response.json()) as { message?: string };
    return body.message;
  } catch {
    return undefined;
  }
}

function resolveEmail(claims: TokenClaims) {
  return claims.email;
}

function resolveUsername(claims: TokenClaims) {
  return claims.preferred_username ?? claims.username ?? claims.email?.split("@")[0];
}

function readStoredAccountType() {
  if (typeof window === "undefined") {
    return "INDIVIDUAL";
  }

  return window.sessionStorage.getItem(accountTypeStorageKey) === "ORGANIZATION"
    ? "ORGANIZATION"
    : "INDIVIDUAL";
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [initialized, setInitialized] = useState(false);
  const [session, setSession] = useState<AuthSession | null>(null);
  const [appUser, setAppUser] = useState<AppUser | null>(null);
  const [error, setError] = useState("");
  const appUserProvisionPromiseRef = useRef<Promise<AppUser> | null>(null);

  const authenticated = Boolean(session);

  const setCurrentSession = useCallback((nextSession: AuthSession | null) => {
    setSession(nextSession);
    storeSession(nextSession);
  }, []);

  useEffect(() => {
    queueMicrotask(() => {
      setSession(readStoredSession());
      setInitialized(true);
    });
  }, []);

  const refreshToken = useCallback(async () => {
    if (!session) {
      throw new Error("User is not authenticated.");
    }

    if (session.expiresAt - Date.now() > 30000) {
      return session.accessToken;
    }

    const refreshed = createSession(
      await requestToken(
        new URLSearchParams({
          client_id: keycloakConfig.clientId,
          grant_type: "refresh_token",
          refresh_token: session.refreshToken,
        }),
      ),
    );

    setCurrentSession(refreshed);
    return refreshed.accessToken;
  }, [session, setCurrentSession]);

  const apiFetch = useCallback(
    async (path: string, init: RequestInit = {}) => {
      const accessToken = await refreshToken();
      const headers = new Headers(init.headers);
      headers.set("Authorization", `Bearer ${accessToken}`);

      if (init.body && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
      }

      return fetch(`${apiBaseUrl}${path}`, {
        ...init,
        headers,
      });
    },
    [refreshToken],
  );

  const provisionAppUser = useCallback(
    async (sessionToUse: AuthSession, accountType: "INDIVIDUAL" | "ORGANIZATION" = readStoredAccountType()) => {
      if (appUser) {
        return appUser;
      }

      if (appUserProvisionPromiseRef.current) {
        return appUserProvisionPromiseRef.current;
      }

      const provisionPromise = (async () => {
        const email = resolveEmail(sessionToUse.claims);
        const username = resolveUsername(sessionToUse.claims);

        if (!email || !username) {
          throw new Error("The Keycloak token is missing email or username claims.");
        }

        const meResponse = await fetch(`${apiBaseUrl}/api/users/me`, {
          headers: {
            Authorization: `Bearer ${sessionToUse.accessToken}`,
          },
        });

        if (meResponse.ok) {
          const currentUser = (await meResponse.json()) as AppUser;
          setAppUser(currentUser);
          return currentUser;
        }

        if (meResponse.status !== 404) {
          throw new Error(`Failed to load current app user (${meResponse.status}).`);
        }

        const createResponse = await fetch(`${apiBaseUrl}/api/users`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${sessionToUse.accessToken}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ email, username, accountType }),
        });

        if (!createResponse.ok) {
          throw new Error(`Failed to create app user (${createResponse.status}).`);
        }

        const createdUser = (await createResponse.json()) as AppUser;
        setAppUser(createdUser);
        return createdUser;
      })();

      appUserProvisionPromiseRef.current = provisionPromise;

      try {
        return await provisionPromise;
      } finally {
        appUserProvisionPromiseRef.current = null;
      }
    },
    [appUser],
  );

  const ensureAppUser = useCallback(async () => {
    if (!session) {
      setAppUser(null);
      return null;
    }

    return provisionAppUser(session, readStoredAccountType());
  }, [provisionAppUser, session]);

  const refreshAppUser = useCallback(async () => {
    if (!session) {
      setAppUser(null);
      return null;
    }

    const accessToken = await refreshToken();
    const response = await fetch(`${apiBaseUrl}/api/users/me`, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    if (!response.ok) {
      throw new Error(`Failed to refresh current app user (${response.status}).`);
    }

    const currentUser = (await response.json()) as AppUser;
    setAppUser(currentUser);
    return currentUser;
  }, [refreshToken, session]);

  const login = useCallback(
    async (username: string, password: string, accountType: "INDIVIDUAL" | "ORGANIZATION" = readStoredAccountType()) => {
      setError("");

      const nextSession = createSession(
        await requestToken(
          new URLSearchParams({
            client_id: keycloakConfig.clientId,
            grant_type: "password",
            username,
            password,
            scope: "openid profile email",
          }),
        ),
      );

      setCurrentSession(nextSession);
      window.sessionStorage.setItem(accountTypeStorageKey, accountType);

      return provisionAppUser(nextSession, accountType);
    },
    [provisionAppUser, setCurrentSession],
  );

  const register = useCallback(
    async (email: string, password: string, accountType: "INDIVIDUAL" | "ORGANIZATION") => {
      setError("");

      const response = await fetch(`${apiBaseUrl}/api/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password, accountType }),
      });

      if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
          throw new Error("Account creation is not available right now. Restart the app with the latest code.");
        }
        throw new Error((await readErrorMessage(response)) ?? `Account creation failed (${response.status}).`);
      }

      window.sessionStorage.setItem(accountTypeStorageKey, accountType);
      return login(email, password, accountType);
    },
    [login],
  );

  const logout = useCallback(async () => {
    const refreshTokenValue = session?.refreshToken;
    setAppUser(null);
    setCurrentSession(null);

    if (refreshTokenValue) {
      await fetch(logoutUrl(), {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
          client_id: keycloakConfig.clientId,
          refresh_token: refreshTokenValue,
        }),
      }).catch(() => undefined);
    }
  }, [session?.refreshToken, setCurrentSession]);

  useEffect(() => {
    if (!initialized || !session || appUser) {
      return;
    }

    void Promise.resolve()
      .then(() => ensureAppUser())
      .catch(() => {
        setError("");
        setCurrentSession(null);
      });
  }, [appUser, ensureAppUser, initialized, session, setCurrentSession]);

  const value = useMemo<AuthContextValue>(
    () => ({
      initialized,
      authenticated,
      appUser,
      token: session?.accessToken,
      error,
      login,
      register,
      logout,
      refreshToken,
      apiFetch,
      ensureAppUser,
      refreshAppUser,
    }),
    [initialized, authenticated, appUser, session?.accessToken, error, login, register, logout, refreshToken, apiFetch, ensureAppUser, refreshAppUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider.");
  }

  return context;
}
