package com.agora.agoracampus.user.core.service;

import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.dto.response.AppUserResponse;
import com.agora.agoracampus.user.core.dto.request.CreateAppUserRequest;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.profile.organization.repository.OrganizationProfileRepository;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import com.agora.agoracampus.security.SecurityIdentityService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final SecurityIdentityService securityIdentityService;
    private final ProfileRepository profileRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final OrganizationProfileRepository organizationProfileRepository;

    public AppUserService(
            AppUserRepository appUserRepository,
            SecurityIdentityService securityIdentityService,
            ProfileRepository profileRepository,
            IndividualProfileRepository individualProfileRepository,
            OrganizationProfileRepository organizationProfileRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.securityIdentityService = securityIdentityService;
        this.profileRepository = profileRepository;
        this.individualProfileRepository = individualProfileRepository;
        this.organizationProfileRepository = organizationProfileRepository;
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

        AppUser savedUser = appUserRepository.save(appUser);
        ensureDefaultProfile(savedUser, resolveAccountType(request.accountType()));
        return toResponse(savedUser);
    }

    @Transactional
    public Optional<AppUserResponse> findCurrentUser() {
        String keycloakId = securityIdentityService.requireKeycloakSubject();
        return appUserRepository.findByKeycloakId(keycloakId)
                .map(user -> {
                    ensureDefaultProfile(user, ProfileType.INDIVIDUAL);
                    return toResponse(user);
                });
    }

    @Transactional
    public List<AppUserResponse> findAllUsers() {
        return appUserRepository.findAll().stream()
                .peek(user -> ensureDefaultProfile(user, ProfileType.INDIVIDUAL))
                .map(this::toResponse)
                .toList();
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

    private ProfileType resolveAccountType(String requestedAccountType) {
        if ("ORGANIZATION".equals(requestedAccountType)) {
            return ProfileType.ORGANIZATION;
        }
        return ProfileType.INDIVIDUAL;
    }

    private void ensureDefaultProfile(AppUser appUser, ProfileType profileType) {
        if (profileRepository.findByAppUser_Id(appUser.getId()).isPresent()) {
            return;
        }

        Profile profile = new Profile();
        profile.setAppUser(appUser);
        profile.setProfileType(profileType);
        profile.setHeadline(profileType == ProfileType.ORGANIZATION ? "Organization" : "Agora Campus member");
        profile.setDescription("");
        profile.setLocation("Agora Campus");
        Profile savedProfile = profileRepository.save(profile);

        if (profileType == ProfileType.ORGANIZATION) {
            OrganizationProfile organizationProfile = new OrganizationProfile();
            organizationProfile.setProfile(savedProfile);
            organizationProfile.setOrganizationName(displayName(appUser));
            organizationProfile.setIndustry("Education");
            organizationProfile.setSpecialties("Campus opportunities");
            savedProfile.setOrganizationProfile(organizationProfile);
            organizationProfileRepository.save(organizationProfile);
            return;
        }

        IndividualProfile individualProfile = new IndividualProfile();
        individualProfile.setProfile(savedProfile);
        individualProfile.setFirstName(firstName(appUser.getUsername()));
        individualProfile.setLastName("Member");
        savedProfile.setIndividualProfile(individualProfile);
        individualProfileRepository.save(individualProfile);
    }

    private AppUserResponse toResponse(AppUser appUser) {
        Profile profile = profileRepository.findByAppUser_Id(appUser.getId()).orElse(null);
        String accountType = null;
        Long profileId = null;
        Long individualProfileId = null;
        Long organizationProfileId = null;
        String displayName = appUser.getUsername();

        if (profile != null) {
            accountType = profile.getProfileType().name();
            profileId = profile.getId();
            if (profile.getIndividualProfile() != null) {
                individualProfileId = profile.getIndividualProfile().getId();
                displayName = profile.getIndividualProfile().getFirstName() + " "
                        + profile.getIndividualProfile().getLastName();
            }
            if (profile.getOrganizationProfile() != null) {
                organizationProfileId = profile.getOrganizationProfile().getId();
                displayName = profile.getOrganizationProfile().getOrganizationName();
            }
        }

        return new AppUserResponse(
                appUser.getId(),
                appUser.getKeycloakId(),
                appUser.getEmail(),
                appUser.getUsername(),
                appUser.getCreatedAt(),
                accountType,
                profileId,
                individualProfileId,
                organizationProfileId,
                displayName
        );
    }

    private String displayName(AppUser appUser) {
        return appUser.getUsername() == null || appUser.getUsername().isBlank()
                ? appUser.getEmail()
                : appUser.getUsername();
    }

    private String firstName(String username) {
        if (username == null || username.isBlank()) {
            return "Agora";
        }
        String clean = username.replaceAll("[^A-Za-z0-9]", " ").trim();
        if (clean.length() < 2) {
            return "Agora";
        }
        return clean.substring(0, 1).toUpperCase() + clean.substring(1);
    }
}
