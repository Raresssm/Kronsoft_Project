package com.agora.agoracampus.dto;

public record VolunteeringResponse(
        Long volunteeringId,
        String cause,
        String schedule,
        String benefits
) {
}
