package com.agora.agoracampus.dto.response;

import com.agora.agoracampus.models.OpportunityType;

import java.time.Instant;

public record OpportunityResponse(
        Long opportunityId,
        Long postedByUserId,
        OpportunityType type,
        String title,
        String location,
        String period,
        String description,
        String additionalInfo,
        Instant createdAt,
        PostingProfileResponse postingProfile,
        VolunteeringResponse volunteering
) {
}
