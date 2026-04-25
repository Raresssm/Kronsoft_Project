package com.agora.agoracampus.dto.response;

import java.time.Instant;

public record AppUserResponse(
        Long id,
        String keycloakId,
        String email,
        String username,
        Instant createdAt
) {
}
