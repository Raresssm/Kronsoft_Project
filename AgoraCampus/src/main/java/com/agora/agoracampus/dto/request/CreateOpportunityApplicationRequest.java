package com.agora.agoracampus.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateOpportunityApplicationRequest(
        @NotNull Long applicantUserId
) {
}
