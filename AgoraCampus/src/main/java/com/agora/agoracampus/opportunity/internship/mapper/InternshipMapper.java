package com.agora.agoracampus.opportunity.internship.mapper;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.internship.dto.request.CreateInternshipRequest;
import com.agora.agoracampus.opportunity.internship.dto.request.UpdateInternshipRequest;
import com.agora.agoracampus.opportunity.internship.dto.response.InternshipResponse;
import com.agora.agoracampus.opportunity.internship.model.Internship;
import org.springframework.stereotype.Component;

@Component
public class InternshipMapper {

    public Internship toEntity(CreateInternshipRequest request, Opportunity opportunity) {
        return Internship.builder()
                .opportunity(opportunity)
                .duration(request.getDuration())
                .compensation(request.getCompensation())
                .requirements(request.getRequirements())
                .build();
    }

    public void updateEntity(Internship internship, UpdateInternshipRequest request) {
        internship.setDuration(request.getDuration());
        internship.setCompensation(request.getCompensation());
        internship.setRequirements(request.getRequirements());
    }

    public InternshipResponse toResponse(Internship internship) {
        return InternshipResponse.builder()
                .internshipId(internship.getId())
                .opportunityId(internship.getOpportunity().getId())
                .duration(internship.getDuration())
                .compensation(internship.getCompensation())
                .requirements(internship.getRequirements())
                .build();
    }
}
