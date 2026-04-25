package com.agora.agoracampus.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import  com.agora.agoracampus.dto.request.CreateCompetitionRequest;
import  com.agora.agoracampus.dto.request.UpdateCompetitionRequest;
import  com.agora.agoracampus.dto.response.CompetitionResponse;
import  com.agora.agoracampus.service.CompetitionService;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    // GET /api/competitions
    @GetMapping
    public List<CompetitionResponse> getAll() {
        return competitionService.findAll();
    }

    // GET /api/competitions/{id}
    @GetMapping("/{id}")
    public CompetitionResponse getById(@PathVariable Integer id) {
        return competitionService.findById(id);
    }

    // GET /api/competitions/by-opportunity/{opportunityId}
    @GetMapping("/by-opportunity/{opportunityId}")
    public List<CompetitionResponse> getByOpportunity(@PathVariable Integer opportunityId) {
        return competitionService.findByOpportunityId(opportunityId);
    }

    // GET /api/competitions/expired
    @GetMapping("/expired")
    public List<CompetitionResponse> getExpired() {
        return competitionService.findExpired();
    }

    // POST /api/competitions
    @PostMapping
    public ResponseEntity<CompetitionResponse> create(
            @Valid @RequestBody CreateCompetitionRequest request) {
        CompetitionResponse created = competitionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/competitions/{id}
    @PutMapping("/{id}")
    public CompetitionResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCompetitionRequest request) {
        return competitionService.update(id, request);
    }

    // DELETE /api/competitions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        competitionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
