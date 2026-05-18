package com.agora.agoracampus.opportunity.studentproject.service;

import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.studentproject.dto.response.StudentProjectResponse;
import com.agora.agoracampus.opportunity.studentproject.mapper.StudentProjectMapper;
import com.agora.agoracampus.opportunity.studentproject.model.StudentProject;
import com.agora.agoracampus.opportunity.studentproject.repository.StudentProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentProjectService {

    private final StudentProjectRepository studentProjectRepository;
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
}
