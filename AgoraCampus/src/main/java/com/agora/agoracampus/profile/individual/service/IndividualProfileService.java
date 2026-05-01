package com.agora.agoracampus.profile.individual.service;

import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.core.service.ProfileService;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileCreateRequest;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileUpdateRequest;
import com.agora.agoracampus.profile.individual.mapper.IndividualProfileMapper;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndividualProfileService {

    private final IndividualProfileRepository individualProfileRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final IndividualProfileMapper individualProfileMapper;
    private final AppUserRepository appUserRepository;


    public IndividualProfileResponse getByProfileId(Long profileId) {
        IndividualProfile profile = individualProfileRepository.findById(profileId).orElseThrow(() -> new RuntimeException("Individual profile not found"));
        return individualProfileMapper.toResponse(profile);
    }

    public IndividualProfileResponse create(IndividualProfileCreateRequest dto) {
        AppUser appUser = appUserRepository.findById(dto.appUserId()).orElseThrow(() -> new NotFoundException("User not found"));
        Profile profile = new Profile();
        profile.setAppUser(appUser);
        profile.setHeadline(dto.headline());
        profile.setDescription(dto.description());
        profile.setLocation(dto.location());
        profile.setWebsite(dto.website());
        Profile savedProfile = profileRepository.save(profile);

        IndividualProfile ind = new IndividualProfile();
        ind.setProfile(savedProfile);
        ind.setFirstName(dto.firstName());
        ind.setLastName(dto.lastName());
        ind.setPhone(dto.phone());

        return individualProfileMapper.toResponse(ind);
    }

    public List<IndividualProfileResponse> getProfileByName(String name) {
        List<IndividualProfile> existing =
                individualProfileRepository.findProfileByName(name);

        if (existing.isEmpty()) {
            throw new NotFoundException("Individual profile not found");
        }

        return existing.stream()
                .map(individualProfileMapper::toResponse)
                .toList();
    }

    public List<IndividualProfileResponse> getProfileByLocation(String location) {

        List<IndividualProfile> existing =
                individualProfileRepository.findProfileByLocation(location);

        if (existing.isEmpty()) {
            throw new NotFoundException("Individual profile not found");
        }

        return existing.stream()
                .map(individualProfileMapper::toResponse)
                .toList();


    }


    public IndividualProfileResponse update( Long id, IndividualProfileUpdateRequest individualProfile) {
            IndividualProfile existing =  individualProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("Individual profile not found"));

            Profile profile= existing.getProfile();


            individualProfileMapper.updateEntity(existing,individualProfile,profile);
            return individualProfileMapper.toResponse(individualProfileRepository.save(existing));
        }

        public void  delete(Long profileId) {
            IndividualProfile existing = individualProfileRepository.findById(profileId).orElseThrow(() -> new NotFoundException("Individual profile not found"));

            Profile profile =existing.getProfile();
            profileService.deleteProfile(profile.getId());

        }

}
