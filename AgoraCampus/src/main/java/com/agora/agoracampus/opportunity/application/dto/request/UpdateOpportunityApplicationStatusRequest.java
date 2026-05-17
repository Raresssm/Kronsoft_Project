package com.agora.agoracampus.opportunity.application.dto.request;

import com.agora.agoracampus.opportunity.application.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOpportunityApplicationStatusRequest(
        @NotNull ApplicationStatus status
) {
}