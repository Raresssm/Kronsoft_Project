package com.agora.agoracampus.profile.organization.mapper;

import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.core.dto.request.ProfileUpdateRequest;
import com.agora.agoracampus.profile.core.mapper.ProfileMapper;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.profile.organization.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.profile.organization.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.profile.organization.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrganizationProfileMapper {

   private final ProfileRepository profileRepository;

   private final    ProfileMapper profileMapper;
    public OrganizationProfileResponse toResponse(OrganizationProfile org) {
        return new OrganizationProfileResponse(
                org.getId(),
                org.getProfile().getId(),
                org.getProfile().getHeadline(),
                org.getProfile().getDescription(),
                org.getProfile().getLocation(),
                org.getProfile().getWebsite(),
                org.getProfile().getProfilePicture(),
                org.getProfile().getCoverImage(),
                org.getProfile().getUpdatedAt(),
                org.getOrganizationName(),
                org.getPhone(),
                org.getIndustry(),
                org.getSpecialties()
        );
    }


    public OrganizationProfile toEntity(CreateOrganizationProfileRequest dto, Profile profile) {
        OrganizationProfile org = new OrganizationProfile();
        org.setProfile(profile);
        org.setOrganizationName(dto.organizationName());
        org.setPhone(dto.phone());
        org.setSpecialties(dto.specialties());
        org.setIndustry(dto.industry());
        return org;
    }


    public void updateEntity(OrganizationProfile org, OrganizationProfileUpdateRequest dto) {

   Profile profile=org.getProfile();
        if (dto.website() != null) profile.setWebsite(dto.website());
        if (dto.profilePicture() != null) profile.setProfilePicture(dto.profilePicture());
        if(dto.coverImage()!=null) profile.setCoverImage(dto.coverImage());
        if(dto.headline()!=null) profile.setHeadline(dto.headline());
        if(dto.location()!=null) profile.setLocation(dto.location());
        if(dto.description()!=null) profile.setDescription(dto.description());


        if (dto.organizationName() != null) org.setOrganizationName(dto.organizationName());
        if (dto.phone() != null) org.setPhone(dto.phone());
        if (dto.specialties() != null) org.setSpecialties(dto.specialties());
        if (dto.industry() != null) org.setIndustry(dto.industry());

    }
}
