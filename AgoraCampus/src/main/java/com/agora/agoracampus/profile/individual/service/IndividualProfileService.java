package com.agora.agoracampus.profile.individual.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.core.mapper.ProfileMapper;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileActorRole;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.profile.core.service.ProfileService;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileCreateRequest;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileUpdateRequest;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.mapper.IndividualProfileMapper;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
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

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class IndividualProfileService {

    private final IndividualProfileRepository individualProfileRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final IndividualProfileMapper individualProfileMapper;
    private final ProfileMapper profileMapper;
    private final AppUserRepository appUserRepository;



    public IndividualProfileResponse getByProfileId(Long profileId, Long actingUserId) {
        resolveActorRole(actingUserId);
        IndividualProfile profile = individualProfileRepository
                .findById(profileId).orElseThrow(() -> new NotFoundException("Individual profile not found"));
        return individualProfileMapper.toResponse(profile);
    }


    public List<IndividualProfileResponse> getByTitle(String title) {
        return individualProfileRepository.findAll()
                .stream()
                .filter(individualProfile ->
                        individualProfile.getBackgrounds()
                                .stream()
                                .anyMatch(background ->
                                        background.getTitle() != null &&
                                                background.getTitle().toLowerCase()
                                                        .contains(title.toLowerCase())
                                )
                )
                .map(individualProfileMapper::toResponse)
                .toList();
    }

    @Transactional
    public IndividualProfileResponse create(Long actingUserId, IndividualProfileCreateRequest dto) {
        resolveActorRole(actingUserId);

        if (!actingUserId.equals(dto.appUserId())) {
            throw new BadRequestException("Acting user must be the same as the user creating the profile.");
        }

        AppUser appUser = appUserRepository.findById(dto.appUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Profile profile = new Profile();
        profile.setAppUser(appUser);
        profile.setHeadline(dto.headline());
        profile.setDescription(dto.description());
        profile.setLocation(dto.location());
        profile.setWebsite(dto.website());
        profile.setProfileType(ProfileType.INDIVIDUAL);
        Profile savedProfile = profileRepository.save(profile);

        IndividualProfile ind = new IndividualProfile();
        ind.setProfile(savedProfile);
        ind.setFirstName(dto.firstName());
        ind.setLastName(dto.lastName());
        ind.setPhone(dto.phone());

        return individualProfileMapper.toResponse(
                individualProfileRepository.save(ind));
    }

    @Transactional
    public IndividualProfileResponse update(Long profileId, Long actingUserId, IndividualProfileUpdateRequest dto) {
        ProfileActorRole actorRole = resolveActorRole(actingUserId);
        IndividualProfile existing = individualProfileRepository
                .findById(profileId)
                .orElseThrow(() -> new NotFoundException("Individual profile not found"));

        requireAdminOrOwner(
                actorRole,
                actingUserId,
                existing.getProfile().getAppUser().getId(),
                "Only admins or the profile owner can update this profile."
        );

       // profileMapper.updateEntity(existing.getProfile(), dto);
        profileRepository.save(existing.getProfile());

        individualProfileMapper.updateEntity(existing, dto);
        return individualProfileMapper.toResponse(
                individualProfileRepository.save(existing));
    }

    @Transactional
    public void delete(Long profileId, Long actingUserId) {
        ProfileActorRole actorRole = resolveActorRole(actingUserId);
        IndividualProfile existing = individualProfileRepository
                .findById(profileId).orElseThrow(() -> new NotFoundException("Individual profile not found"));

        requireAdminOrOwner(
                actorRole,
                actingUserId,
                existing.getProfile().getAppUser().getId(),
                "Only admins or the profile owner can delete this profile."
        );

        profileService.deleteProfile(existing.getProfile().getId(), actingUserId);
    }

    public List<IndividualProfileResponse> getProfileByName(String name) {
        List<IndividualProfile> existing = individualProfileRepository.findProfileByName(name);
        if (existing.isEmpty()) {
            throw new NotFoundException("Individual profile not found");
        }
        return existing.stream()
                .map(individualProfileMapper::toResponse)
                .toList();
    }

    public List<IndividualProfileResponse> getProfileByLocation(String location) {
        List<IndividualProfile> existing = individualProfileRepository.findProfileByLocation(location);
        if (existing.isEmpty()) {
            throw new NotFoundException("Individual profile not found");
        }
        return existing.stream()
                .map(individualProfileMapper::toResponse)
                .toList();
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

        if (isAdmin) {
            return ProfileActorRole.ADMIN;
        }

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