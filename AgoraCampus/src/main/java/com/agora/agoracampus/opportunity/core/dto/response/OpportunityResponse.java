package com.agora.agoracampus.opportunity.core.dto.response;

import com.agora.agoracampus.opportunity.core.model.OpportunityType;
import com.agora.agoracampus.profile.core.dto.response.PostingProfileResponse;
import com.agora.agoracampus.opportunity.volunteering.dto.response.VolunteeringResponse;

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
