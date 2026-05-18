package com.agora.agoracampus.opportunity.studentproject.controller;

import com.agora.agoracampus.opportunity.studentproject.dto.response.StudentProjectResponse;
import com.agora.agoracampus.opportunity.studentproject.service.StudentProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opportunities/{opportunityId}/student-project")
@RequiredArgsConstructor
public class StudentProjectOpportunityController {

    private final StudentProjectService studentProjectService;

    @GetMapping
    public StudentProjectResponse getByOpportunity(@PathVariable Long opportunityId) {
        return studentProjectService.getByOpportunityId(opportunityId);
    }
}
