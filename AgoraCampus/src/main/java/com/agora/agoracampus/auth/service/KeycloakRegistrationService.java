package com.agora.agoracampus.auth.service;

import com.agora.agoracampus.auth.dto.request.RegisterRequest;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakRegistrationService {

    private final RestClient restClient;
    private final String serverUrl;
    private final String realm;
    private final String adminUsername;
    private final String adminPassword;

    public KeycloakRegistrationService(
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.admin.username:admin}") String adminUsername,
            @Value("${keycloak.admin.password:admin}") String adminPassword
    ) {
        this.restClient = RestClient.create();
        this.serverUrl = trimTrailingSlash(serverUrl);
        this.realm = realm;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public void register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = email.substring(0, email.indexOf("@"));
        String accountType = request.accountType();
        String adminToken = fetchAdminToken();

        try {
            restClient.post()
                    .uri(URI.create(serverUrl + "/admin/realms/" + realm + "/users"))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "username", username,
                            "email", email,
                            "firstName", username,
                            "lastName", accountType.toLowerCase(),
                            "enabled", true,
                            "emailVerified", true,
                            "requiredActions", List.of(),
                            "attributes", Map.of("accountType", List.of(accountType)),
                            "credentials", List.of(Map.of(
                                    "type", "password",
                                    "value", request.password(),
                                    "temporary", false
                            ))
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
                throw new ConflictException("An account with this email already exists.");
            }
            throw new BadRequestException("Could not create the Keycloak account.");
        }
    }

    private String fetchAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", "admin-cli");
        form.add("grant_type", "password");
        form.add("username", adminUsername);
        form.add("password", adminPassword);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(URI.create(serverUrl + "/realms/master/protocol/openid-connect/token"))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            Object accessToken = response == null ? null : response.get("access_token");
            if (accessToken instanceof String token && !token.isBlank()) {
                return token;
            }
        } catch (RestClientResponseException exception) {
            throw new BadRequestException("Could not authenticate with Keycloak admin.");
        }

        throw new BadRequestException("Keycloak admin token was missing.");
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
