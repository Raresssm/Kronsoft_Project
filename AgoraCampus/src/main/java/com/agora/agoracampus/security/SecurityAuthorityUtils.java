package com.agora.agoracampus.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Locale;

public final class SecurityAuthorityUtils {

    private SecurityAuthorityUtils() {
    }

    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public static boolean hasAdminAuthority(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String authorityName = authority.getAuthority();
            if (authorityName == null) {
                continue;
            }
            String normalizedAuthority = authorityName.toUpperCase(Locale.ROOT);
            if ("ROLE_ADMIN".equals(normalizedAuthority) || "ADMIN".equals(normalizedAuthority)) {
                return true;
            }
        }
        return false;
    }
}
