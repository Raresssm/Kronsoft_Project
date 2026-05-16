package com.agora.agoracampus.opportunity.core.dto.request;

import com.agora.agoracampus.opportunity.competition.dto.request.CompetitionDetailsRequest;
import com.agora.agoracampus.opportunity.core.model.OpportunityType;
import com.agora.agoracampus.opportunity.internship.dto.request.InternshipDetailsRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.request.StudentProjectDetailsRequest;
import com.agora.agoracampus.opportunity.volunteering.dto.request.VolunteeringDetailsRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOpportunityRequest(
        @NotNull Long postedByUserId,
        Long organizationProfileId,
        Long individualProfileId,
        @NotNull OpportunityType type,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String location,
        @NotBlank @Size(max = 100) String period,
        @NotBlank String description,
        @Size(max = 1000) String additionalInfo,
        @Valid VolunteeringDetailsRequest volunteering,
        @Valid CompetitionDetailsRequest competition,
        @Valid InternshipDetailsRequest internship,
        @Valid StudentProjectDetailsRequest studentProject
) {
}
