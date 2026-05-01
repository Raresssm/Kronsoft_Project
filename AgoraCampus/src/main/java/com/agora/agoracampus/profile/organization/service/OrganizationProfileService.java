package com.agora.agoracampus.profile.organization.service;

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


    public OrganizationProfileResponse getByProfileId(Long profileId) {
        OrganizationProfile org = organizationProfileRepository
                .findByProfile_Id(profileId)
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
        Profile profile = profileRepository.findByAppUser_Id(dto.appUserId())
                .orElseThrow(() -> new NotFoundException("Profile not found for app user id: " + dto.appUserId()));

        if (profile.getOrganizationProfile() != null) {
            throw new BadRequestException("Organization profile already exists for this profile");
        }

        OrganizationProfile org = organizationProfileMapper.toEntity(dto, profile);
        return organizationProfileMapper.toResponse(organizationProfileRepository.save(org));
    }


    public OrganizationProfileResponse update(Long profileId, OrganizationProfileUpdateRequest dto) {
        OrganizationProfile existing = organizationProfileRepository
                .findByProfile_Id(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));

        organizationProfileMapper.updateEntity(existing, dto);
        return organizationProfileMapper.toResponse(organizationProfileRepository.save(existing));
    }

    // DELETE
    public void delete(Long profileId) {
        OrganizationProfile existing = organizationProfileRepository
                .findByProfile_Id(profileId)
                .orElseThrow(() -> new NotFoundException("Organization profile not found with profile id: " + profileId));
        organizationProfileRepository.delete(existing);
    }
}
