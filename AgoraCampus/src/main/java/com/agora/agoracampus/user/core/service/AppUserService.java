package com.agora.agoracampus.user.core.service;

import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.dto.response.AppUserResponse;
import com.agora.agoracampus.user.core.dto.request.CreateAppUserRequest;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import com.agora.agoracampus.security.SecurityIdentityService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final SecurityIdentityService securityIdentityService;

    public AppUserService(AppUserRepository appUserRepository, SecurityIdentityService securityIdentityService) {
        this.appUserRepository = appUserRepository;
        this.securityIdentityService = securityIdentityService;
    }

    @Transactional
    public AppUserResponse createUser(CreateAppUserRequest request) {
        String keycloakId = securityIdentityService.requireKeycloakSubject();
        String email = resolveEmail(request.email());
        String username = resolveUsername(request.username());

        if (appUserRepository.existsByKeycloakId(keycloakId)) {
            throw new ConflictException("A user with the same Keycloak ID already exists.");
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new ConflictException("A user with the same email already exists.");
        }
        if (appUserRepository.existsByUsername(username)) {
            throw new ConflictException("A user with the same username already exists.");
        }

        AppUser appUser = new AppUser();
        appUser.setKeycloakId(keycloakId);
        appUser.setEmail(email);
        appUser.setUsername(username);

        return toResponse(appUserRepository.save(appUser));
    }

    public Optional<AppUserResponse> findCurrentUser() {
        String keycloakId = securityIdentityService.requireKeycloakSubject();
        return appUserRepository.findByKeycloakId(keycloakId).map(this::toResponse);
    }

    public AppUser getRequiredEntity(Long appUserId) {
        return appUserRepository.findById(appUserId)
                .orElseThrow(() -> new NotFoundException("App user " + appUserId + " was not found."));
    }

    private String resolveEmail(String requestedEmail) {
        if (requestedEmail != null && !requestedEmail.isBlank()) {
            securityIdentityService.findEmailClaim().ifPresent(tokenEmail -> {
                if (!tokenEmail.equalsIgnoreCase(requestedEmail)) {
                    throw new BadRequestException("Email must match the authenticated Keycloak account.");
                }
            });
            return requestedEmail;
        }
        return securityIdentityService.findEmailClaim()
                .orElseThrow(() -> new BadRequestException("Email is required when it is not present in the JWT."));
    }

    private String resolveUsername(String requestedUsername) {
        if (requestedUsername != null && !requestedUsername.isBlank()) {
            return requestedUsername;
        }
        return securityIdentityService.findPreferredUsernameClaim()
                .orElseThrow(() -> new BadRequestException(
                        "Username is required when preferred_username is not present in the JWT."));
    }

    private AppUserResponse toResponse(AppUser appUser) {
        return new AppUserResponse(
                appUser.getId(),
                appUser.getKeycloakId(),
                appUser.getEmail(),
                appUser.getUsername(),
                appUser.getCreatedAt()
        );
    }
}
