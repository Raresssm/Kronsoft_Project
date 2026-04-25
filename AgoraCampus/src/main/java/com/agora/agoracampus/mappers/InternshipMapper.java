package com.agora.agoracampus.mappers;

import org.springframework.stereotype.Component;

import com.agora.agoracampus.dto.request.CreateInternshipRequest;
import com.agora.agoracampus.dto.request.UpdateInternshipRequest;
import com.agora.agoracampus.dto.response.InternshipResponse;
import com.agora.agoracampus.models.Internship;



@Component
public class InternshipMapper {

    public Internship toEntity(CreateInternshipRequest request) {
        return Internship.builder()
                .opportunityId(request.getOpportunityId())
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
                .internshipId(internship.getInternshipId())
                .opportunityId(internship.getOpportunityId())
                .duration(internship.getDuration())
                .compensation(internship.getCompensation())
                .requirements(internship.getRequirements())
                .build();
    }
}
