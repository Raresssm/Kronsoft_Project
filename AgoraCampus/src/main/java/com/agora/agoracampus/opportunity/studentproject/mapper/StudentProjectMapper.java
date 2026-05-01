package com.agora.agoracampus.opportunity.studentproject.mapper;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.studentproject.dto.request.CreateStudentProjectRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.request.UpdateStudentProjectRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.response.StudentProjectResponse;
import com.agora.agoracampus.opportunity.studentproject.model.StudentProject;
import org.springframework.stereotype.Component;

@Component
public class StudentProjectMapper {

    public StudentProject toEntity(CreateStudentProjectRequest request, Opportunity opportunity) {
        StudentProject entity = new StudentProject();
        entity.setOpportunity(opportunity);
        entity.setProjectDomain(request.projectDomain());
        entity.setRequiredSkills(request.requiredSkills());
        entity.setTeamSize(request.teamSize());
        return entity;
    }

    public void updateEntity(StudentProject entity, UpdateStudentProjectRequest request) {
        if (request.projectDomain() != null) {
            entity.setProjectDomain(request.projectDomain());
        }
        if (request.requiredSkills() != null) {
            entity.setRequiredSkills(request.requiredSkills());
        }
        if (request.teamSize() != null) {
            entity.setTeamSize(request.teamSize());
        }
    }

    public StudentProjectResponse toResponse(StudentProject entity) {
        return new StudentProjectResponse(
                entity.getId(),
                entity.getOpportunity().getId(),
                entity.getProjectDomain(),
                entity.getRequiredSkills(),
                entity.getTeamSize()
        );
    }
}
