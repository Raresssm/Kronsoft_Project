package com.agora.agoracampus.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.agora.agoracampus.dto.request.CreateInternshipRequest;
import com.agora.agoracampus.dto.request.UpdateInternshipRequest;
import com.agora.agoracampus.dto.response.InternshipResponse;
import com.agora.agoracampus.service.InternshipService;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
public class InternshipController {

    private final InternshipService internshipService;

    // GET /api/internships
    @GetMapping
    public List<InternshipResponse> getAll() {
        return internshipService.findAll();
    }

    // GET /api/internships/{id}
    @GetMapping("/{id}")
    public InternshipResponse getById(@PathVariable Integer id) {
        return internshipService.findById(id);
    }

    // GET /api/internships/by-opportunity/{opportunityId}
    @GetMapping("/by-opportunity/{opportunityId}")
    public List<InternshipResponse> getByOpportunity(@PathVariable Integer opportunityId) {
        return internshipService.findByOpportunityId(opportunityId);
    }

    // POST /api/internships
    @PostMapping
    public ResponseEntity<InternshipResponse> create(
            @Valid @RequestBody CreateInternshipRequest request) {
        InternshipResponse created = internshipService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/internships/{id}
    @PutMapping("/{id}")
    public InternshipResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateInternshipRequest request) {
        return internshipService.update(id, request);
    }

    // DELETE /api/internships/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        internshipService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
