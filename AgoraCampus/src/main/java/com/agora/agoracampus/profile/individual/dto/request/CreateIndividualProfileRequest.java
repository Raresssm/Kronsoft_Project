package com.agora.agoracampus.profile.individual.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIndividualProfileRequest(
        @NotNull Long appUserId,
        @Size(max = 255) String header,
        String description,
        @Size(max = 255) String location,
        @Size(max = 512) String website,
        @Size(max = 512) String profilePicture,
        @Size(max = 512) String coverImage,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 50) String phone,
        @Size(max = 512) String cvDocument,
        @Size(max = 255) String education,
        @Size(max = 100) String educationPeriod,
        @Size(max = 255) String workExperience,
        @Size(max = 255) String otherProjects
) {
}
