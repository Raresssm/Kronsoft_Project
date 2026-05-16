package com.agora.agoracampus.opportunity.internship.controller;

import com.agora.agoracampus.opportunity.internship.dto.response.InternshipResponse;
import com.agora.agoracampus.opportunity.internship.service.InternshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
public class InternshipController {

    private final InternshipService internshipService;

    @GetMapping
    public List<InternshipResponse> getAll() {
        return internshipService.findAll();
    }

    @GetMapping("/{id}")
    public InternshipResponse getById(@PathVariable Long id) {
        return internshipService.findById(id);
    }

    @GetMapping("/by-opportunity/{opportunityId}")
    public InternshipResponse getByOpportunity(@PathVariable Long opportunityId) {
        return internshipService.findByOpportunityId(opportunityId);
    }
}
