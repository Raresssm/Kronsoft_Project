export const keycloakConfig = {
  url: process.env.NEXT_PUBLIC_KEYCLOAK_URL ?? "http://localhost:8090",
  realm: process.env.NEXT_PUBLIC_KEYCLOAK_REALM ?? "agora-campus",
  clientId: process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID ?? "agora-frontend",
};

export const apiBaseUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
