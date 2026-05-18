package com.agora.agoracampus.opportunity.studentproject.mapper;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.studentproject.dto.request.StudentProjectDetailsRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.response.StudentProjectResponse;
import com.agora.agoracampus.opportunity.studentproject.model.StudentProject;
import org.springframework.stereotype.Component;

@Component
public class StudentProjectMapper {

    public StudentProject toEntity(StudentProjectDetailsRequest details, Opportunity opportunity) {
        StudentProject entity = new StudentProject();
        entity.setOpportunity(opportunity);
        entity.setProjectDomain(details.projectDomain());
        entity.setRequiredSkills(details.requiredSkills());
        entity.setTeamSize(details.teamSize());
        return entity;
    }

    public StudentProjectResponse toResponse(StudentProject entity) {
        if (entity == null) {
            return null;
        }
        return new StudentProjectResponse(
                entity.getId(),
                entity.getOpportunity().getId(),
                entity.getProjectDomain(),
                entity.getRequiredSkills(),
                entity.getTeamSize()
        );
    }
}
