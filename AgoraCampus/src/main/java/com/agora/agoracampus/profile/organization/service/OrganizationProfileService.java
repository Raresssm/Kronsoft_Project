package com.agora.agoracampus.profile.organization.service;

import com.agora.agoracampus.profile.core.service.ProfileService;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.organization.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.profile.organization.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.profile.organization.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.organization.mapper.OrganizationProfileMapper;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.organization.repository.OrganizationProfileRepository;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationProfileService {
    private final OrganizationProfileRepository organizationProfileRepository;
    private final ProfileRepository profileRepository;
    private final OrganizationProfileMapper organizationProfileMapper;
    private final AppUserRepository appUserRepository;
    private final ProfileService profileService;


    public OrganizationProfileResponse getByProfileId(Long profileId) {
        OrganizationProfile org = organizationProfileRepository
                .findByProfile_Id(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));
        return organizationProfileMapper.toResponse(org);
    }


    public List<OrganizationProfileResponse> searchByName(String name) {


        List<OrganizationProfile> existing =
                organizationProfileRepository.findByOrganizationNameContainingIgnoreCase(name);

        if (existing.isEmpty()) {
            throw new NotFoundException("Organization profile not found");
        }

        return organizationProfileRepository
                .findByOrganizationNameContainingIgnoreCase(name)
                .stream()
                .map(organizationProfileMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrganizationProfileResponse> searchByLocation(String location) {

        List<OrganizationProfile> existing =
                organizationProfileRepository.findByOrganizationLocationContainingIgnoreCase(location);

        if (existing.isEmpty()) {
            throw new NotFoundException("Organization profile not found");
        }
        return organizationProfileRepository
                .findByOrganizationLocationContainingIgnoreCase(location)
                .stream()
                .map(organizationProfileMapper::toResponse)
                .collect(Collectors.toList());
    }
    public List<OrganizationProfileResponse> searchByIndustry(String industry) {
        List<OrganizationProfile> existing =
                organizationProfileRepository.findByOrganizationIndustryContainingIgnoreCase(industry);

        if (existing.isEmpty()) {
            throw new NotFoundException("Organization profile not found");
        }

        return organizationProfileRepository
                .findByOrganizationIndustryContainingIgnoreCase(industry)
                .stream()
                .map(organizationProfileMapper::toResponse)
                .collect(Collectors.toList());
    }
    public List<OrganizationProfileResponse> searchBySpecialties(String specialties) {

        List<OrganizationProfile> existing =
                organizationProfileRepository.findByOrganizationSpecialtiesContainingIgnoreCase(specialties);

        if (existing.isEmpty()) {
            throw new NotFoundException("Organization profile not found");
        }
        return organizationProfileRepository
                .findByOrganizationLocationContainingIgnoreCase(specialties)
                .stream()
                .map(organizationProfileMapper::toResponse)
                .collect(Collectors.toList());

    }


    public OrganizationProfileResponse create(CreateOrganizationProfileRequest dto) {
        AppUser appUser = appUserRepository.findById(dto.appUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Profile profile = new Profile();
        profile.setAppUser(appUser);
        profile.setHeadline(dto.headline());
        profile.setDescription(dto.description());
        profile.setLocation(dto.location());
        profile.setWebsite(dto.website());
        Profile savedProfile = profileRepository.save(profile);


        OrganizationProfile org = new OrganizationProfile();
        org.setProfile(savedProfile);
        org.setOrganizationName(dto.organizationName());
        org.setPhone(dto.phone());
        org.setIndustry(dto.industry());
        org.setSpecialties(dto.specialties());
        organizationProfileRepository.save(org);

        return organizationProfileMapper.toResponse(org);
    }



    public OrganizationProfileResponse update(Long profileId, OrganizationProfileUpdateRequest dto) {

        OrganizationProfile existing = organizationProfileRepository
                .findByProfile_Id(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));


        Profile profile = existing.getProfile();
        organizationProfileMapper.updateEntity(existing, dto,profile);
        return organizationProfileMapper.toResponse(organizationProfileRepository.save(existing));
    }



    public void delete(Long profileId) {
        OrganizationProfile existing = organizationProfileRepository
                .findByProfile_Id(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));

        Profile profile =existing.getProfile();
        profileService.deleteProfile(profile.getId());

    }
}
