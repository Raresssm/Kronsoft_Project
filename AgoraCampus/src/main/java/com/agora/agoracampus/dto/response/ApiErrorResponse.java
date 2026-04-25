package com.agora.agoracampus.dto.response;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message
) {
}
