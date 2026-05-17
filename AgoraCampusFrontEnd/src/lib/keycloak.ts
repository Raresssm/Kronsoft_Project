import Keycloak from "keycloak-js";
import { appConfig } from "./config";

let instance: Keycloak | null = null;

export function getKeycloak(): Keycloak {
  if (!instance) {
    instance = new Keycloak({
      url: appConfig.keycloakUrl,
      realm: appConfig.keycloakRealm,
      clientId: appConfig.keycloakClientId,
    });
  }
  return instance;
}
