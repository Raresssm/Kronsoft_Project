package com.agora.agoracampus.user.core.dto.response;

import java.time.Instant;

public record AppUserResponse(
        Long id,
        String keycloakId,
        String email,
        String username,
        Instant createdAt,
        String accountType,
        Long profileId,
        Long individualProfileId,
        Long organizationProfileId,
        String displayName
) {
}
