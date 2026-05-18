package com.agora.agoracampus.opportunity.studentproject.dto.response;

public record StudentProjectResponse(
        Long projectId,
        Long opportunityId,
        String projectDomain,
        String requiredSkills,
        Integer teamSize
) {}
