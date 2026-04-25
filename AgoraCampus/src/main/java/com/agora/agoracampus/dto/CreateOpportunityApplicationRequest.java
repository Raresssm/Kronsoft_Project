package com.agora.agoracampus.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOpportunityApplicationRequest(
        @NotNull Long applicantUserId
) {
}
