package com.agora.agoracampus.opportunity.competition.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CompetitionDetailsRequest(
        @NotBlank @Size(max = 200) String theme,
        @Size(max = 300) String eligibility,
        @Size(max = 200) String prize,
        @NotNull LocalDate deadline
) {
}
