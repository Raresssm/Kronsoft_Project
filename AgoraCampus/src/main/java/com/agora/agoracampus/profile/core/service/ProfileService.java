package com.agora.agoracampus.profile.core.service;


import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.core.dto.request.ProfileUpdateRequest;
import com.agora.agoracampus.profile.core.dto.response.ProfileResponse;
import com.agora.agoracampus.profile.core.mapper.ProfileMapper;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileActorRole;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;


@Service
    @RequiredArgsConstructor
    public class ProfileService {

        private final ProfileRepository profileRepository;
        private final AppUserRepository appUserRepository;
        private final ProfileMapper mapper;

        public ProfileResponse getProfileById(Long profileId, Long actingUserId) {
            validateUserExists(actingUserId);
            Profile profile = profileRepository.findById(profileId)
                    .orElseThrow(() -> new NotFoundException("Profile not found with id: " + profileId));
            return mapper.toResponse(profile);
        }

        @Transactional
        public ProfileResponse updateProfile(Long profileId, Long actingUserId, ProfileUpdateRequest dto) {
            ProfileActorRole actorRole = resolveActorRole(actingUserId);
            Profile existing = profileRepository.findById(profileId)
                    .orElseThrow(() -> new NotFoundException("Profile not found with id: " + profileId));

            requireAdminOrOwner(
                    actorRole,
                    actingUserId,
                    existing.getAppUser().getId(),
                    "Only admins or the profile owner can update this profile."
            );

            mapper.updateEntity(existing, dto);
            return mapper.toResponse(profileRepository.save(existing));
        }

        @Transactional
        public void deleteProfile(Long profileId, Long actingUserId) {
            ProfileActorRole actorRole = resolveActorRole(actingUserId);
            Profile existing = profileRepository.findById(profileId)
                    .orElseThrow(() -> new NotFoundException("Profile not found with id: " + profileId));


            requireAdminOrOwner(
                    actorRole,
                    actingUserId,
                    existing.getAppUser().getId(),
                    "Only admins or the profile owner can delete this profile."
            );

            profileRepository.delete(existing);
        }

        // ==================== PRIVATE ====================

        private AppUser validateUserExists(Long userId) {
            return appUserRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User " + userId + " was not found."));
        }

        private ProfileActorRole resolveActorRole(Long actingUserId) {
            validateUserExists(actingUserId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean authenticated = isAuthenticated(authentication);
            boolean isAdmin = authenticated && hasAdminAuthority(authentication);

            if (authenticated) {
                validateAuthenticatedIdentity(actingUserId, isAdmin, authentication);
            }

            if (isAdmin) {
                return ProfileActorRole.ADMIN;
            }


            Profile profile = profileRepository.findByAppUser_Id(actingUserId)
                    .orElseThrow(() -> new BadRequestException(
                            "User " + actingUserId + " has no profile."
                    ));

            if (profile.getIndividualProfile() != null) {
                return ProfileActorRole.INDIVIDUAL;
            }
            if (profile.getOrganizationProfile() != null) {
                return ProfileActorRole.ORGANIZATION;
            }

            throw new BadRequestException("User " + actingUserId + " has unsupported profile type.");
        }

        private void requireAdminOrOwner(
                ProfileActorRole actorRole,
                Long actingUserId,
                Long ownerId,
                String message
        ) {
            if (actorRole != ProfileActorRole.ADMIN && !actingUserId.equals(ownerId)) {
                throw new BadRequestException(message);
            }
        }

        private boolean isAuthenticated(Authentication authentication) {
            return authentication != null
                    && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken);
        }

        private boolean hasAdminAuthority(Authentication authentication) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String normalizedAuthority = authority.getAuthority().toUpperCase(Locale.ROOT);
                if ("ROLE_ADMIN".equals(normalizedAuthority) || "ADMIN".equals(normalizedAuthority)) {
                    return true;
                }
            }
            return false;
        }

        private void validateAuthenticatedIdentity(
                Long actingUserId,
                boolean isAdmin,
                Authentication authentication
        ) {
            if (isAdmin) return;

            String principalName = authentication.getName();
            if (principalName == null || principalName.isBlank() || "anonymousUser".equals(principalName)) {
                return;
            }

            AppUser authenticatedUser = appUserRepository.findByKeycloakId(principalName)
                    .orElseThrow(() -> new BadRequestException(
                            "Authenticated principal is not registered as an application user."
                    ));

            if (!authenticatedUser.getId().equals(actingUserId)) {
                throw new BadRequestException("actingUserId must match the authenticated user.");
            }
        }
    }