package com.agora.agoracampus.profile.core.dto.response;

import com.agora.agoracampus.profile.core.model.ProfileType;

public record PostingProfileResponse(
        ProfileType profileType,
        Long organizationProfileId,
        Long individualProfileId,
        Long profileId,
        Long appUserId,
        String displayName
) {
}
