package com.agora.agoracampus.dto.response;

public record VolunteeringResponse(
        Long volunteeringId,
        String cause,
        String schedule,
        String benefits
) {
}
