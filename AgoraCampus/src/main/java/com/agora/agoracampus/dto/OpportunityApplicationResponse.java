package com.agora.agoracampus.dto;

import com.agora.agoracampus.domain.ApplicationStatus;
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
