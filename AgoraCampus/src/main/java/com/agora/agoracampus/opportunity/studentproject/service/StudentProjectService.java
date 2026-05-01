package com.agora.agoracampus.opportunity.studentproject.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.core.model.OpportunityType;
import com.agora.agoracampus.opportunity.core.repository.OpportunityRepository;
import com.agora.agoracampus.opportunity.studentproject.dto.request.CreateStudentProjectRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.request.UpdateStudentProjectRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.response.StudentProjectResponse;
import com.agora.agoracampus.opportunity.studentproject.mapper.StudentProjectMapper;
import com.agora.agoracampus.opportunity.studentproject.model.StudentProject;
import com.agora.agoracampus.opportunity.studentproject.repository.StudentProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentProjectService {

    private final StudentProjectRepository studentProjectRepository;
    private final OpportunityRepository opportunityRepository;
    private final StudentProjectMapper studentProjectMapper;

    public StudentProjectResponse getByOpportunityId(Long opportunityId) {
        StudentProject project = studentProjectRepository.findByOpportunity_Id(opportunityId)
                .orElseThrow(() -> new NotFoundException(
                        "Student project for opportunity " + opportunityId + " was not found."
                ));
        return studentProjectMapper.toResponse(project);
    }

    public List<StudentProjectResponse> findAll() {
        return studentProjectRepository.findAll().stream().map(studentProjectMapper::toResponse).toList();
    }

    public StudentProjectResponse findById(Long id) {
        return studentProjectMapper.toResponse(studentProjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student project not found.")));
    }

    @Transactional
    public StudentProjectResponse create(CreateStudentProjectRequest request) {
        var opportunity = opportunityRepository.findById(request.opportunityId())
                .orElseThrow(() -> new NotFoundException("Opportunity " + request.opportunityId() + " was not found."));
        if (opportunity.getType() != OpportunityType.STUDENT_PROJECT) {
            throw new BadRequestException("Opportunity type must be STUDENT_PROJECT for student project details.");
        }
        if (studentProjectRepository.findByOpportunity_Id(request.opportunityId()).isPresent()) {
            throw new BadRequestException("A student project already exists for this opportunity.");
        }

        StudentProject saved = studentProjectRepository.save(studentProjectMapper.toEntity(request, opportunity));
        return studentProjectMapper.toResponse(saved);
    }

    @Transactional
    public StudentProjectResponse update(Long id, UpdateStudentProjectRequest request) {
        StudentProject existing = studentProjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student project not found."));
        studentProjectMapper.updateEntity(existing, request);
        return studentProjectMapper.toResponse(studentProjectRepository.save(existing));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!studentProjectRepository.existsById(id)) {
            throw new NotFoundException("Student project not found.");
        }
        studentProjectRepository.deleteById(id);
    }
}
