package com.agora.agoracampus.opportunity.application.controller;

import com.agora.agoracampus.opportunity.application.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.opportunity.application.dto.response.OpportunityApplicationResponse;
import com.agora.agoracampus.opportunity.application.service.OpportunityApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opportunity-applications")
@RequiredArgsConstructor
public class OpportunityApplicationController {

    private final OpportunityApplicationService opportunityApplicationService;

    @GetMapping
    public List<OpportunityApplicationResponse> getAll() {
        return opportunityApplicationService.findAll();
    }

    @GetMapping("/{id}")
    public OpportunityApplicationResponse getById(@PathVariable Long id) {
        return opportunityApplicationService.findById(id);
    }

    @GetMapping("/by-opportunity/{opportunityId}")
    public List<OpportunityApplicationResponse> getByOpportunity(@PathVariable Long opportunityId) {
        return opportunityApplicationService.findByOpportunityId(opportunityId);
    }

    @PostMapping("/by-opportunity/{opportunityId}")
    public ResponseEntity<OpportunityApplicationResponse> create(
            @PathVariable Long opportunityId,
            @Valid @RequestBody CreateOpportunityApplicationRequest request) {
        OpportunityApplicationResponse created = opportunityApplicationService.create(opportunityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        opportunityApplicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
