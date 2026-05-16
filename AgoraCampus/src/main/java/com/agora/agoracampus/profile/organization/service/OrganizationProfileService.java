package com.agora.agoracampus.profile.organization.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileActorRole;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.profile.core.service.ProfileService;
import com.agora.agoracampus.profile.organization.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.profile.organization.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.profile.organization.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.profile.organization.mapper.OrganizationProfileMapper;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.profile.organization.repository.OrganizationProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.security.SecurityAuthorityUtils;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationProfileService {

    private final OrganizationProfileRepository organizationProfileRepository;
    private final ProfileRepository profileRepository;
    private final OrganizationProfileMapper organizationProfileMapper;
    private final AppUserRepository appUserRepository;
    private final ProfileService profileService;



    public OrganizationProfileResponse getByProfileId(Long profileId, Long actingUserId) {
        resolveActorRole(actingUserId);
        OrganizationProfile org = organizationProfileRepository
                .findById(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));
        return organizationProfileMapper.toResponse(org);
    }

    public List<OrganizationProfileResponse> searchByName(String name) {
        List<OrganizationProfile> existing = organizationProfileRepository
                .findByOrganizationNameContainingIgnoreCase(name);
        if (existing.isEmpty()) throw new NotFoundException("Organization profile not found");
        return existing.stream()
                .map(organizationProfileMapper::toResponse)
                .toList();
    }

    public List<OrganizationProfileResponse> searchByLocation(String location) {
        List<OrganizationProfile> existing = organizationProfileRepository
                .findByLocationContainingIgnoreCase(location);
        if (existing.isEmpty()) throw new NotFoundException("Organization profile not found");
        return existing.stream()
                .map(organizationProfileMapper::toResponse)
                .toList();
    }


    public List<OrganizationProfileResponse> searchByIndustry(String industry) {
        List<OrganizationProfile> existing = organizationProfileRepository
                .findByIndustryContainingIgnoreCase(industry);
        if (existing.isEmpty()) throw new NotFoundException("Organization profile not found");
        return existing.stream()
                .map(organizationProfileMapper::toResponse)
                .toList();
    }

    public List<OrganizationProfileResponse> searchBySpecialties(String specialties) {
        List<OrganizationProfile> existing = organizationProfileRepository
                .findBySpecialtiesContainingIgnoreCase(specialties);
        if (existing.isEmpty()) throw new NotFoundException("Organization profile not found");
        return existing.stream()
                .map(organizationProfileMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrganizationProfileResponse create(Long actingUserId, CreateOrganizationProfileRequest dto) {
        resolveActorRole(actingUserId);

        if (!actingUserId.equals(dto.appUserId())) {
            throw new BadRequestException("Acting user must be the same as the user creating the profile.");
        }

        AppUser appUser = appUserRepository.findById(dto.appUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (profileRepository.findByAppUser_Id(dto.appUserId()).isPresent()) {
            throw new ConflictException("User already has a profile.");
        }

        Profile profile = new Profile();
        profile.setAppUser(appUser);
        profile.setHeadline(dto.headline());
        profile.setDescription(dto.description());
        profile.setLocation(dto.location());
        profile.setWebsite(dto.website());
        profile.setProfileType(ProfileType.ORGANIZATION);
        profile.setProfilePicture(dto.profilePicture());
        profile.setCoverImage(dto.coverImage());
        Profile savedProfile = profileRepository.save(profile);

        OrganizationProfile org = new OrganizationProfile();
        org.setProfile(savedProfile);
        org.setOrganizationName(dto.organizationName());
        org.setPhone(dto.phone());
        org.setIndustry(dto.industry());
        org.setSpecialties(dto.specialties());
        savedProfile.setOrganizationProfile(org);

        return organizationProfileMapper.toResponse(
                organizationProfileRepository.save(org));
    }

    @Transactional
    public OrganizationProfileResponse update(Long profileId, Long actingUserId, OrganizationProfileUpdateRequest dto) {
        ProfileActorRole actorRole = resolveActorRole(actingUserId);
        OrganizationProfile existing = organizationProfileRepository
                .findByProfile_Id(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found"));

        requireAdminOrOwner(
                actorRole,
                actingUserId,
                existing.getProfile().getAppUser().getId(),
                "Only admins or the profile owner can update this profile."
        );

        organizationProfileMapper.updateEntity(existing, dto);
        return organizationProfileMapper.toResponse(
                organizationProfileRepository.save(existing));
    }

    @Transactional
    public void delete(Long profileId, Long actingUserId) {
        ProfileActorRole actorRole = resolveActorRole(actingUserId);
        OrganizationProfile existing = organizationProfileRepository
                .findByProfile_Id(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found"));

        requireAdminOrOwner(
                actorRole,
                actingUserId,
                existing.getProfile().getAppUser().getId(),
                "Only admins or the profile owner can delete this profile."
        );

        profileService.deleteProfile(existing.getProfile().getId(),actingUserId);
    }

    private void validateUserExists(Long userId) {
        appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " was not found."));
    }

    private ProfileActorRole resolveActorRole(Long actingUserId) {
        validateUserExists(actingUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = SecurityAuthorityUtils.isAuthenticated(authentication);
        boolean isAdmin = authenticated && SecurityAuthorityUtils.hasAdminAuthority(authentication);

        if (authenticated) {
            validateAuthenticatedIdentity(actingUserId, isAdmin, authentication);
        }

        if (isAdmin) return ProfileActorRole.ADMIN;
        return ProfileActorRole.ORGANIZATION;
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
