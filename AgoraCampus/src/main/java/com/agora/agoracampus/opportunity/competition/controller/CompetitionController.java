package com.agora.agoracampus.opportunity.competition.controller;

import com.agora.agoracampus.opportunity.competition.dto.request.CreateCompetitionRequest;
import com.agora.agoracampus.opportunity.competition.dto.request.UpdateCompetitionRequest;
import com.agora.agoracampus.opportunity.competition.dto.response.CompetitionResponse;
import com.agora.agoracampus.opportunity.competition.service.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping
    public List<CompetitionResponse> getAll() {
        return competitionService.findAll();
    }

    @GetMapping("/{id}")
    public CompetitionResponse getById(@PathVariable Long id) {
        return competitionService.findById(id);
    }

    @GetMapping("/by-opportunity/{opportunityId}")
    public CompetitionResponse getByOpportunity(@PathVariable Long opportunityId) {
        return competitionService.findByOpportunityId(opportunityId);
    }

    @GetMapping("/expired")
    public List<CompetitionResponse> getExpired() {
        return competitionService.findExpired();
    }

    @PostMapping
    public ResponseEntity<CompetitionResponse> create(
            @Valid @RequestBody CreateCompetitionRequest request) {
        CompetitionResponse created = competitionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CompetitionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompetitionRequest request) {
        return competitionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        competitionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
