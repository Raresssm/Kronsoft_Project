package com.agora.agoracampus.opportunity.application.dto.response;

import com.agora.agoracampus.opportunity.application.model.ApplicationStatus;

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
