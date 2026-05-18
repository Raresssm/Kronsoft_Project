package com.agora.agoracampus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Profile("!test")
public class KeycloakJwtConfiguration {

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthorityPrefix("SCOPE_");

        Converter<Jwt, Collection<GrantedAuthority>> realmRolesConverter = jwt -> {
            Object realmAccess = jwt.getClaim("realm_access");
            if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
                return Collections.emptyList();
            }
            Object roles = realmAccessMap.get("roles");
            if (!(roles instanceof Collection<?> roleNames)) {
                return Collections.emptyList();
            }
            return roleNames.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(this::toRoleAuthority)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toCollection(ArrayList::new));
        };

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Collection<GrantedAuthority> scopes = scopeConverter.convert(jwt);
            if (!scopes.isEmpty()) {
                authorities.addAll(scopes);
            }
            authorities.addAll(realmRolesConverter.convert(jwt));
            return authorities;
        });
        return authenticationConverter;
    }

    private String toRoleAuthority(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}
