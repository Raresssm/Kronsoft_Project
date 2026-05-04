package com.agora.agoracampus.profile.individual.dto.response;

import com.agora.agoracampus.profile.background.dto.response.BackgroundResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

public record IndividualProfileResponse(
        Long id,

        Long profileId,

        String headline,
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
        List<BackgroundResponse> backgrounds

) {}
