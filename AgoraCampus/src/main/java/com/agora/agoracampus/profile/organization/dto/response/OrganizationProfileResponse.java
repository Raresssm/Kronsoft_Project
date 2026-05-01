package com.agora.agoracampus.profile.organization.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;




public record OrganizationProfileResponse(
        String headline,
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
) {}
