package com.agora.agoracampus.mappers;

import com.agora.agoracampus.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.models.OrganizationProfile;
import com.agora.agoracampus.models.Profile;
import org.springframework.stereotype.Component;


@Component
public class OrganizationProfileMapper {


    public OrganizationProfileResponse toResponse(OrganizationProfile org) {
        return new OrganizationProfileResponse(
                org.getId().longValue(),
                org.getProfile().getId().longValue(),
                org.getProfile().getAppUser().getId().longValue(),
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
        if (dto.organizationName() != null) org.setOrganizationName(dto.organizationName());
        if (dto.phone() != null) org.setPhone(dto.phone());
        if (dto.specialties() != null) org.setSpecialties(dto.specialties());
        if (dto.industry() != null) org.setIndustry(dto.industry());
    }
}
