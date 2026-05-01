package com.agora.agoracampus.opportunity.studentproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStudentProjectRequest(
        @NotNull Long opportunityId,

        @NotBlank
        @Size(max = 255)
        String projectDomain,

        String requiredSkills,

        Integer teamSize
) {}
