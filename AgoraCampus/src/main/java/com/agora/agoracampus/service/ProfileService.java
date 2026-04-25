package com.agora.agoracampus.service;

import com.agora.agoracampus.domain.AppUser;
import com.agora.agoracampus.domain.IndividualProfile;
import com.agora.agoracampus.domain.OrganizationProfile;
import com.agora.agoracampus.domain.Profile;
import com.agora.agoracampus.domain.ProfileType;
import com.agora.agoracampus.dto.CreateIndividualProfileRequest;
import com.agora.agoracampus.dto.CreateOrganizationProfileRequest;
import com.agora.agoracampus.dto.IndividualProfileResponse;
import com.agora.agoracampus.dto.OrganizationProfileResponse;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.repository.IndividualProfileRepository;
import com.agora.agoracampus.repository.OrganizationProfileRepository;
import com.agora.agoracampus.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final AppUserService appUserService;
    private final ProfileRepository profileRepository;
    private final OrganizationProfileRepository organizationProfileRepository;
    private final IndividualProfileRepository individualProfileRepository;

    public ProfileService(
            AppUserService appUserService,
            ProfileRepository profileRepository,
            OrganizationProfileRepository organizationProfileRepository,
            IndividualProfileRepository individualProfileRepository
    ) {
        this.appUserService = appUserService;
        this.profileRepository = profileRepository;
        this.organizationProfileRepository = organizationProfileRepository;
        this.individualProfileRepository = individualProfileRepository;
    }

    @Transactional
    public OrganizationProfileResponse createOrganizationProfile(CreateOrganizationProfileRequest request) {
        AppUser appUser = appUserService.getRequiredEntity(request.appUserId());
        ensureUserDoesNotAlreadyHaveProfile(appUser.getId());

        Profile profile = createBaseProfile(
                appUser,
                ProfileType.ORGANIZATION,
                request.header(),
                request.description(),
                request.location(),
                request.website(),
                request.profilePicture(),
                request.coverImage()
        );
        profileRepository.save(profile);

        OrganizationProfile organizationProfile = new OrganizationProfile();
        organizationProfile.setProfile(profile);
        organizationProfile.setOrganizationName(request.organizationName());
        organizationProfile.setPhone(request.phone());
        organizationProfile.setIndustry(request.industry());
        organizationProfile.setSpecialties(request.specialties());

        OrganizationProfile savedProfile = organizationProfileRepository.save(organizationProfile);
        profile.setOrganizationProfile(savedProfile);
        appUser.setProfile(profile);

        return toOrganizationResponse(savedProfile);
    }

    @Transactional
    public IndividualProfileResponse createIndividualProfile(CreateIndividualProfileRequest request) {
        AppUser appUser = appUserService.getRequiredEntity(request.appUserId());
        ensureUserDoesNotAlreadyHaveProfile(appUser.getId());

        Profile profile = createBaseProfile(
                appUser,
                ProfileType.INDIVIDUAL,
                request.header(),
                request.description(),
                request.location(),
                request.website(),
                request.profilePicture(),
                request.coverImage()
        );
        profileRepository.save(profile);

        IndividualProfile individualProfile = new IndividualProfile();
        individualProfile.setProfile(profile);
        individualProfile.setFirstName(request.firstName());
        individualProfile.setLastName(request.lastName());
        individualProfile.setPhone(request.phone());
        individualProfile.setCvDocument(request.cvDocument());
        individualProfile.setEducation(request.education());
        individualProfile.setEducationPeriod(request.educationPeriod());
        individualProfile.setWorkExperience(request.workExperience());
        individualProfile.setOtherProjects(request.otherProjects());

        IndividualProfile savedProfile = individualProfileRepository.save(individualProfile);
        profile.setIndividualProfile(savedProfile);
        appUser.setProfile(profile);

        return toIndividualResponse(savedProfile);
    }

    private void ensureUserDoesNotAlreadyHaveProfile(Long appUserId) {
        if (profileRepository.existsByAppUserId(appUserId)) {
            throw new ConflictException("App user " + appUserId + " already has a profile.");
        }
    }

    private Profile createBaseProfile(
            AppUser appUser,
            ProfileType profileType,
            String header,
            String description,
            String location,
            String website,
            String profilePicture,
            String coverImage
    ) {
        Profile profile = new Profile();
        profile.setAppUser(appUser);
        profile.setHeader(header);
        profile.setDescription(description);
        profile.setLocation(location);
        profile.setWebsite(website);
        profile.setProfilePicture(profilePicture);
        profile.setCoverImage(coverImage);
        profile.setProfileType(profileType);
        return profile;
    }

    private OrganizationProfileResponse toOrganizationResponse(OrganizationProfile organizationProfile) {
        Profile profile = organizationProfile.getProfile();
        return new OrganizationProfileResponse(
                organizationProfile.getId(),
                profile.getId(),
                profile.getAppUser().getId(),
                profile.getHeader(),
                profile.getDescription(),
                profile.getLocation(),
                profile.getWebsite(),
                profile.getProfilePicture(),
                profile.getCoverImage(),
                profile.getUpdatedAt(),
                organizationProfile.getOrganizationName(),
                organizationProfile.getPhone(),
                organizationProfile.getIndustry(),
                organizationProfile.getSpecialties()
        );
    }

    private IndividualProfileResponse toIndividualResponse(IndividualProfile individualProfile) {
        Profile profile = individualProfile.getProfile();
        return new IndividualProfileResponse(
                individualProfile.getId(),
                profile.getId(),
                profile.getAppUser().getId(),
                profile.getHeader(),
                profile.getDescription(),
                profile.getLocation(),
                profile.getWebsite(),
                profile.getProfilePicture(),
                profile.getCoverImage(),
                profile.getUpdatedAt(),
                individualProfile.getFirstName(),
                individualProfile.getLastName(),
                individualProfile.getPhone(),
                individualProfile.getCvDocument(),
                individualProfile.getEducation(),
                individualProfile.getEducationPeriod(),
                individualProfile.getWorkExperience(),
                individualProfile.getOtherProjects()
        );
    }
}
