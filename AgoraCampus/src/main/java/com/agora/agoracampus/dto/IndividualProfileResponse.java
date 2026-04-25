package com.agora.agoracampus.dto;

import java.time.Instant;

public record IndividualProfileResponse(
        Long individualProfileId,
        Long profileId,
        Long appUserId,
        String header,
        String description,
        String location,
        String website,
        String profilePicture,
        String coverImage,
        Instant updatedAt,
        String firstName,
        String lastName,
        String phone,
        String cvDocument,
        String education,
        String educationPeriod,
        String workExperience,
        String otherProjects
) {
}
