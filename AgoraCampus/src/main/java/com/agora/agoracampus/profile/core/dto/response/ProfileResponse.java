package com.agora.agoracampus.profile.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;



public record ProfileResponse(
        Long profileId,
        Long appUserId,
        String headline,
        String description,
        String location,
        String website,
        String profilePicture,
        String coverImage,
        Instant updatedAt
) {}

