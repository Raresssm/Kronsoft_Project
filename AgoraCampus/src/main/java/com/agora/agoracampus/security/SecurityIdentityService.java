package com.agora.agoracampus.security;

import com.agora.agoracampus.exception.BadRequestException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityIdentityService {

    public String requireKeycloakSubject() {
        Authentication authentication = requireAuthentication();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new BadRequestException("JWT subject is missing.");
            }
            return subject;
        }

        String principalName = authentication.getName();
        if (principalName == null || principalName.isBlank() || "anonymousUser".equals(principalName)) {
            throw new BadRequestException("Authentication is required.");
        }
        return principalName;
    }

    public Optional<String> findEmailClaim() {
        return findJwt().map(jwt -> jwt.getClaimAsString("email")).filter(email -> !email.isBlank());
    }

    public Optional<String> findPreferredUsernameClaim() {
        return findJwt()
                .map(jwt -> {
                    String preferredUsername = jwt.getClaimAsString("preferred_username");
                    if (preferredUsername != null && !preferredUsername.isBlank()) {
                        return preferredUsername;
                    }
                    return jwt.getClaimAsString("username");
                })
                .filter(username -> !username.isBlank());
    }

    private Authentication requireAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new BadRequestException("Authentication is required.");
        }
        return authentication;
    }

    private Optional<Jwt> findJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }
}
