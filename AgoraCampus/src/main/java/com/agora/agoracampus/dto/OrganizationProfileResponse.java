package com.agora.agoracampus.dto;

import java.time.Instant;

public record OrganizationProfileResponse(
        Long organizationProfileId,
        Long profileId,
        Long appUserId,
        String header,
        String description,
        String location,
        String website,
        String profilePicture,
        String coverImage,
        Instant updatedAt,
        String organizationName,
        String phone,
        String industry,
        String specialties
) {
}
