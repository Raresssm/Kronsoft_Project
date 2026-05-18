package com.agora.agoracampus.opportunity.competition.controller;

import com.agora.agoracampus.opportunity.competition.dto.response.CompetitionResponse;
import com.agora.agoracampus.opportunity.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
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
}
