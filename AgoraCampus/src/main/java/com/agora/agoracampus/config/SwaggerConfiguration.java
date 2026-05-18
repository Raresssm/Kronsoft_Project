package com.agora.agoracampus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    private final String keycloakServerUrl;
    private final String keycloakRealm;

    public SwaggerConfiguration(
            @Value("${keycloak.server-url:http://localhost:8090}") String keycloakServerUrl,
            @Value("${keycloak.realm:agora-campus}") String keycloakRealm) {
        this.keycloakServerUrl = keycloakServerUrl;
        this.keycloakRealm = keycloakRealm;
    }

    @Bean
    GroupedOpenApi agoraCampusApi() {
        return GroupedOpenApi.builder()
                .group("agora-campus-api")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    OpenAPI openApiDefinition() {
        String authorizeUrl = "%s/realms/%s/protocol/openid-connect/auth"
                .formatted(keycloakServerUrl, keycloakRealm);
        String tokenUrl = "%s/realms/%s/protocol/openid-connect/token"
                .formatted(keycloakServerUrl, keycloakRealm);

        return new OpenAPI()
                .info(new Info()
                        .title("AgoraCampus API")
                        .version("v1")
                        .description("""
                                API secured with Keycloak (JWT Bearer). From Swagger UI, use Authorize → OAuth \
                                Authorization Code / PKCE, or paste a Bearer access token."""))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSecuritySchemes("oauth2",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authorizeUrl)
                                                .tokenUrl(tokenUrl)
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID Connect")
                                                        .addString("profile", "Basic profile claims"))))));
    }
}
