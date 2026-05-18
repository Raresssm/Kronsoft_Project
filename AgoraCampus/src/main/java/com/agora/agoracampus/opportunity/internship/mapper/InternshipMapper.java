package com.agora.agoracampus.opportunity.internship.mapper;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.internship.dto.request.InternshipDetailsRequest;
import com.agora.agoracampus.opportunity.internship.dto.response.InternshipResponse;
import com.agora.agoracampus.opportunity.internship.model.Internship;
import org.springframework.stereotype.Component;

@Component
public class InternshipMapper {

    public Internship toEntity(InternshipDetailsRequest details, Opportunity opportunity) {
        return Internship.builder()
                .opportunity(opportunity)
                .duration(details.duration())
                .compensation(details.compensation())
                .requirements(details.requirements())
                .build();
    }

    public InternshipResponse toResponse(Internship internship) {
        if (internship == null) {
            return null;
        }
        return InternshipResponse.builder()
                .internshipId(internship.getId())
                .opportunityId(internship.getOpportunity().getId())
                .duration(internship.getDuration())
                .compensation(internship.getCompensation())
                .requirements(internship.getRequirements())
                .build();
    }
}
