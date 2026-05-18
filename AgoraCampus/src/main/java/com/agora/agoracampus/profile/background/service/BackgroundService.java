package com.agora.agoracampus.profile.background.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.profile.background.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.profile.background.dto.response.BackgroundResponse;
import com.agora.agoracampus.profile.background.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.background.mapper.BackgroundMapper;
import com.agora.agoracampus.profile.background.model.Background;
import com.agora.agoracampus.profile.core.model.ProfileActorRole;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.background.repository.BackgroundRepository;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;



    @Service
    @RequiredArgsConstructor
    public class BackgroundService {

        private final BackgroundRepository backgroundRepository;
        private final IndividualProfileRepository individualProfileRepository;
        private final BackgroundMapper backgroundMapper;
        private final AppUserRepository appUserRepository;


        public List<BackgroundResponse> getBackgroundsByProfileId(
                Long individualProfileId,
                Long actingUserId) {
            resolveActorRole(actingUserId);
            individualProfileRepository.findById(individualProfileId)
                    .orElseThrow(() -> new NotFoundException("Individual profile not found with id: " + individualProfileId));
            return backgroundRepository.findByIndividualProfile_Id(individualProfileId)
                    .stream()
                    .map(backgroundMapper::toResponse)
                    .toList();
        }

        public BackgroundResponse getBackgroundById(
                Long individualProfileId,
                Long backgroundId,
                Long actingUserId) {
            resolveActorRole(actingUserId);
            Background background = backgroundRepository.findById(backgroundId)
                    .orElseThrow(() -> new NotFoundException("Background not found with id: " + backgroundId));

            if (!background.getIndividualProfile().getId().equals(individualProfileId)) {
                throw new BadRequestException("Background does not belong to this profile.");
            }

            return backgroundMapper.toResponse(background);
        }

        @Transactional
        public BackgroundResponse createBackground(
                Long individualProfileId,
                Long actingUserId,
                BackgroundCreateRequest dto) {
            ProfileActorRole actorRole = resolveActorRole(actingUserId);

            IndividualProfile individualProfile = individualProfileRepository
                    .findById(individualProfileId)
                    .orElseThrow(() -> new NotFoundException("Individual profile not found"));

            requireAdminOrOwner(
                    actorRole,
                    actingUserId,
                    individualProfile.getProfile().getAppUser().getId(),
                    "Only admins or the profile owner can add backgrounds."
            );

            validateDateRange(dto.startDate(), dto.endDate(), dto.currentlyOngoing());
            Background background = backgroundMapper.toEntity(dto, individualProfile);
            return backgroundMapper.toResponse(backgroundRepository.save(background));
        }

        @Transactional
        public BackgroundResponse updateBackground(
                Long individualProfileId,
                Long backgroundId,
                Long actingUserId,
                BackgroundUpdateRequest dto) {
            ProfileActorRole actorRole = resolveActorRole(actingUserId);
            Background existing = backgroundRepository.findById(backgroundId)
                    .orElseThrow(() -> new NotFoundException("Background not found with id: " + backgroundId));

            if (!existing.getIndividualProfile().getId().equals(individualProfileId)) {
                throw new BadRequestException("Background does not belong to this profile.");
            }

            requireAdminOrOwner(
                    actorRole,
                    actingUserId,
                    existing.getIndividualProfile().getProfile().getAppUser().getId(),
                    "Only admins or the profile owner can update backgrounds."
            );

            validateDateRange(dto.startDate(), dto.endDate(), dto.currentlyOngoing());
            backgroundMapper.updateEntity(existing, dto);
            return backgroundMapper.toResponse(backgroundRepository.save(existing));
        }

        @Transactional
        public void deleteBackground(
                Long individualProfileId,
                Long backgroundId,
                Long actingUserId) {
            ProfileActorRole actorRole = resolveActorRole(actingUserId);
            Background existing = backgroundRepository.findById(backgroundId)
                    .orElseThrow(() -> new NotFoundException("Background not found with id: " + backgroundId));

            if (!existing.getIndividualProfile().getId().equals(individualProfileId)) {
                throw new BadRequestException("Background does not belong to this profile.");
            }

            requireAdminOrOwner(
                    actorRole,
                    actingUserId,
                    existing.getIndividualProfile().getProfile().getAppUser().getId(),
                    "Only admins or the profile owner can delete backgrounds."
            );

            backgroundRepository.delete(existing);
        }



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

            if (isAdmin) return ProfileActorRole.ADMIN;
            return ProfileActorRole.INDIVIDUAL;
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

        private void validateDateRange(LocalDate startDate, LocalDate endDate, Boolean currentlyOngoing) {
            if (Boolean.TRUE.equals(currentlyOngoing)) {
                return;
            }
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                throw new BadRequestException("End date cannot be earlier than start date.");
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
            if (principalName == null
                    || principalName.isBlank()
                    || "anonymousUser".equals(principalName)) {
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



