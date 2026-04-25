package com.agora.agoracampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrganizationProfileRequest(
        @NotNull Long appUserId,
        @Size(max = 255) String header,
        String description,
        @Size(max = 255) String location,
        @Size(max = 512) String website,
        @Size(max = 512) String profilePicture,
        @Size(max = 512) String coverImage,
        @NotBlank @Size(max = 255) String organizationName,
        @Size(max = 50) String phone,
        @Size(max = 255) String industry,
        @Size(max = 255) String specialties
) {
}
