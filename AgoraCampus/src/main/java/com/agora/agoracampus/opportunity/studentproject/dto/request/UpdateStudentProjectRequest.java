package com.agora.agoracampus.opportunity.studentproject.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateStudentProjectRequest(
        @Size(max = 255)
        String projectDomain,

        String requiredSkills,

        Integer teamSize
) {}
