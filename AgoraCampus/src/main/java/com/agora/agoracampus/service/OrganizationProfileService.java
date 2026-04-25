package com.agora.agoracampus.service;

import com.agora.agoracampus.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.mappers.OrganizationProfileMapper;
import com.agora.agoracampus.models.OrganizationProfile;
import com.agora.agoracampus.models.Profile;
import com.agora.agoracampus.repository.OrganizationProfileRepository;
import com.agora.agoracampus.repository.ProfileRepository;

import java.util.List;
import java.util.stream.Collectors;

public class OrganizationProfileService {
    private  OrganizationProfileRepository organizationProfileRepository;
    private ProfileRepository profileRepository;
    private  OrganizationProfileMapper organizationProfileMapper;


    public OrganizationProfileResponse getByProfileId(Integer profileId) {
        OrganizationProfile org = organizationProfileRepository
                .findByProfile_ProfileId(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));
        return organizationProfileMapper.toResponse(org);
    }

    public List<OrganizationProfileResponse> searchByName(String name) {
        return organizationProfileRepository
                .findByOrganizationNameContainingIgnoreCase(name)
                .stream()
                .map(organizationProfileMapper::toResponse)
                .collect(Collectors.toList());
    }


    public OrganizationProfileResponse create(CreateOrganizationProfileRequest dto) {
        Profile profile = profileRepository.findById(dto.appUserId().longValue())
                .orElseThrow(() -> new NotFoundException("Profile not found with id: " + dto.appUserId()));

        if (profile.getOrganizationProfile() != null) {
            throw new BadRequestException("Organization profile already exists for this profile");
        }

        OrganizationProfile org = organizationProfileMapper.toEntity(dto, profile);
        return organizationProfileMapper.toResponse(organizationProfileRepository.save(org));
    }


    public OrganizationProfileResponse update(Integer profileId, OrganizationProfileUpdateRequest dto) {
        OrganizationProfile existing = organizationProfileRepository
                .findByProfile_ProfileId(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));

        organizationProfileMapper.updateEntity(existing, dto);
        return organizationProfileMapper.toResponse(organizationProfileRepository.save(existing));
    }

    // DELETE
    public void delete(Integer profileId) {
        OrganizationProfile existing = organizationProfileRepository
                .findByProfile_ProfileId(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));
        organizationProfileRepository.delete(existing);
    }
}
