"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { getKeycloak } from "@/lib/keycloak";
import { applyKeycloakTokens } from "@/lib/keycloak-session";
import type { KeycloakTokenResponse } from "@/lib/auth-types";
import { ensureAppUser, type AppUser } from "@/lib/users";

type AuthStatus = "loading" | "authenticated" | "unauthenticated";

type AuthContextValue = {
  status: AuthStatus;
  appUser: AppUser | null;
  error: string | null;
  loginWithCredentials: (email: string, password: string) => Promise<void>;
  registerWithCredentials: (
    email: string,
    password: string,
    accountType?: string,
    firstName?: string,
    lastName?: string,
  ) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

async function readAuthError(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as { message?: string };
    return body.message ?? "Request failed.";
  } catch {
    return "Request failed.";
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>("loading");
  const [appUser, setAppUser] = useState<AppUser | null>(null);
  const [error, setError] = useState<string | null>(null);

  const syncAppUser = useCallback(async () => {
    const user = await ensureAppUser();
    setAppUser(user);
    setError(null);
    return user;
  }, []);

  const completeSession = useCallback(
    async (tokens: KeycloakTokenResponse) => {
      applyKeycloakTokens(tokens);
      await syncAppUser();
      setStatus("authenticated");
    },
    [syncAppUser],
  );

  useEffect(() => {
    let cancelled = false;

    async function init() {
      const keycloak = getKeycloak();

      try {
        const authenticated = await keycloak.init({
          onLoad: "check-sso",
          pkceMethod: "S256",
          checkLoginIframe: false,
        });

        if (cancelled) {
          return;
        }

        if (!authenticated) {
          setStatus("unauthenticated");
          setAppUser(null);
          return;
        }

        await syncAppUser();

        if (!cancelled) {
          setStatus("authenticated");
        }
      } catch (initError) {
        if (!cancelled) {
          setStatus("unauthenticated");
          setAppUser(null);
          setError(initError instanceof Error ? initError.message : "Authentication failed.");
        }
      }
    }

    void init();

    return () => {
      cancelled = true;
    };
  }, [syncAppUser]);

  const loginWithCredentials = useCallback(
    async (email: string, password: string) => {
      setError(null);
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error(await readAuthError(response));
      }

      const tokens = (await response.json()) as KeycloakTokenResponse;
      await completeSession(tokens);
    },
    [completeSession],
  );

  const registerWithCredentials = useCallback(
    async (
      email: string,
      password: string,
      accountType?: string,
      firstName?: string,
      lastName?: string,
    ) => {
      setError(null);
      const response = await fetch("/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, accountType, firstName, lastName }),
      });

      if (!response.ok) {
        throw new Error(await readAuthError(response));
      }

      const tokens = (await response.json()) as KeycloakTokenResponse;
      await completeSession(tokens);
    },
    [completeSession],
  );

  const logout = useCallback(async () => {
    setAppUser(null);
    setStatus("unauthenticated");
    const keycloak = getKeycloak();
    if (keycloak.authenticated) {
      await keycloak.logout({
        redirectUri: `${window.location.origin}/login`,
      });
    }
  }, []);

  const refreshUser = useCallback(async () => {
    if (!getKeycloak().authenticated) {
      setAppUser(null);
      setStatus("unauthenticated");
      return;
    }
    await syncAppUser();
    setStatus("authenticated");
  }, [syncAppUser]);

  const value = useMemo(
    () => ({
      status,
      appUser,
      error,
      loginWithCredentials,
      registerWithCredentials,
      logout,
      refreshUser,
    }),
    [status, appUser, error, loginWithCredentials, registerWithCredentials, logout, refreshUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
