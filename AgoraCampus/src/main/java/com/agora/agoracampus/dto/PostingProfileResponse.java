package com.agora.agoracampus.dto;

import com.agora.agoracampus.domain.ProfileType;

public record PostingProfileResponse(
        ProfileType profileType,
        Long organizationProfileId,
        Long individualProfileId,
        Long profileId,
        Long appUserId,
        String displayName
) {
}
