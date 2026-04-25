package com.agora.agoracampus.dto.response;

import com.agora.agoracampus.models.ApplicationStatus;

import java.time.Instant;

public record OpportunityApplicationResponse(
        Long applicationId,
        Long opportunityId,
        Long applicantUserId,
        String applicantUsername,
        ApplicationStatus status,
        Instant appliedAt
) {
}
