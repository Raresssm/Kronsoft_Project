package com.agora.agoracampus.dto.response;

import com.agora.agoracampus.models.ProfileType;

public record PostingProfileResponse(
        ProfileType profileType,
        Long organizationProfileId,
        Long individualProfileId,
        Long profileId,
        Long appUserId,
        String displayName
) {
}
