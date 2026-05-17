import type Keycloak from "keycloak-js";
import { getKeycloak } from "./keycloak";
import type { KeycloakTokenResponse } from "./auth-types";

export function applyKeycloakTokens(tokens: KeycloakTokenResponse): void {
  const keycloak = getKeycloak();
  keycloak.token = tokens.access_token;
  keycloak.refreshToken = tokens.refresh_token;
  keycloak.idToken = tokens.id_token;
  keycloak.authenticated = true;
  keycloak.timeSkew = 0;

  if (tokens.access_token) {
    keycloak.tokenParsed = decodeJwt(tokens.access_token);
  }
  if (tokens.refresh_token) {
    keycloak.refreshTokenParsed = decodeJwt(tokens.refresh_token);
  }
  if (tokens.id_token) {
    keycloak.idTokenParsed = decodeJwt(tokens.id_token);
  }
}

function decodeJwt(token: string): Keycloak.KeycloakTokenParsed {
  const payload = token.split(".")[1];
  const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "=");
  const json = atob(padded);
  return JSON.parse(json) as Keycloak.KeycloakTokenParsed;
}

export async function refreshKeycloakToken(): Promise<boolean> {
  const keycloak = getKeycloak();
  if (!keycloak.refreshToken) {
    return false;
  }
  return keycloak.updateToken(30);
}
