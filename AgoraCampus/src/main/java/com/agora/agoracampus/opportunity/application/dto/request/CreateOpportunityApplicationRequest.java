package com.agora.agoracampus.opportunity.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateOpportunityApplicationRequest(
        @NotNull Long applicantUserId
) {
}
