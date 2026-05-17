const keycloakUrl = process.env.NEXT_PUBLIC_KEYCLOAK_URL ?? "http://localhost:8090";
const keycloakRealm = process.env.NEXT_PUBLIC_KEYCLOAK_REALM ?? "agora-campus";
const keycloakClientId = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID ?? "agora-frontend";
const apiUrl = (process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080").replace(/\/$/, "");

export const appConfig = {
  keycloakUrl,
  keycloakRealm,
  keycloakClientId,
  apiUrl,
} as const;
