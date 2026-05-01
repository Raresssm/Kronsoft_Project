package com.agora.agoracampus.opportunity.internship.controller;

import com.agora.agoracampus.opportunity.internship.dto.request.CreateInternshipRequest;
import com.agora.agoracampus.opportunity.internship.dto.request.UpdateInternshipRequest;
import com.agora.agoracampus.opportunity.internship.dto.response.InternshipResponse;
import com.agora.agoracampus.opportunity.internship.service.InternshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<InternshipResponse> create(
            @Valid @RequestBody CreateInternshipRequest request) {
        InternshipResponse created = internshipService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public InternshipResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInternshipRequest request) {
        return internshipService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        internshipService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
