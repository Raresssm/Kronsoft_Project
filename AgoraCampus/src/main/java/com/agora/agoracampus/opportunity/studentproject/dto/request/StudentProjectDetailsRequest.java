package com.agora.agoracampus.opportunity.studentproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentProjectDetailsRequest(
        @NotBlank @Size(max = 255) String projectDomain,
        String requiredSkills,
        Integer teamSize
) {
}
