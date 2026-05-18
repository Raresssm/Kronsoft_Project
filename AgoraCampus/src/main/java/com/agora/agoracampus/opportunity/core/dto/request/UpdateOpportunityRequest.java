package com.agora.agoracampus.opportunity.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOpportunityRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String location,
        @NotBlank @Size(max = 100) String period,
        @NotBlank String description,
        @Size(max = 1000) String additionalInfo
) {
}
