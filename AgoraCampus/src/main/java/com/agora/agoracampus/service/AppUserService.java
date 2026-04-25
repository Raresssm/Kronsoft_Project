package com.agora.agoracampus.service;

import com.agora.agoracampus.domain.AppUser;
import com.agora.agoracampus.dto.AppUserResponse;
import com.agora.agoracampus.dto.CreateAppUserRequest;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUserResponse createUser(CreateAppUserRequest request) {
        if (appUserRepository.existsByKeycloakId(request.keycloakId())) {
            throw new ConflictException("A user with the same Keycloak ID already exists.");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("A user with the same email already exists.");
        }
        if (appUserRepository.existsByUsername(request.username())) {
            throw new ConflictException("A user with the same username already exists.");
        }

        AppUser appUser = new AppUser();
        appUser.setKeycloakId(request.keycloakId());
        appUser.setEmail(request.email());
        appUser.setUsername(request.username());

        return toResponse(appUserRepository.save(appUser));
    }

    public AppUser getRequiredEntity(Long appUserId) {
        return appUserRepository.findById(appUserId)
                .orElseThrow(() -> new NotFoundException("App user " + appUserId + " was not found."));
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
