package com.agora.agoracampus.opportunity.volunteering.dto.response;

public record VolunteeringResponse(
        Long volunteeringId,
        String cause,
        String schedule,
        String benefits
) {
}
