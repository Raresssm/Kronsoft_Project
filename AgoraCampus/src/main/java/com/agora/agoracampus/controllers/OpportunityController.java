package com.agora.agoracampus.controllers;

import com.agora.agoracampus.models.OpportunityType;
import com.agora.agoracampus.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.dto.request.CreateOpportunityRequest;
import com.agora.agoracampus.dto.response.OpportunityApplicationResponse;
import com.agora.agoracampus.dto.response.OpportunityResponse;
import com.agora.agoracampus.service.OpportunityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse createOpportunity(@Valid @RequestBody CreateOpportunityRequest request) {
        return opportunityService.createOpportunity(request);
    }

    @GetMapping
    public List<OpportunityResponse> listOpportunities(
            @RequestParam(required = false) OpportunityType type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long postedByUserId
    ) {
        return opportunityService.listOpportunities(type, location, postedByUserId);
    }

    @GetMapping("/{opportunityId}")
    public OpportunityResponse getOpportunity(@PathVariable Long opportunityId) {
        return opportunityService.getOpportunity(opportunityId);
    }

    @PostMapping("/{opportunityId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityApplicationResponse applyToOpportunity(
            @PathVariable Long opportunityId,
            @Valid @RequestBody CreateOpportunityApplicationRequest request
    ) {
        return opportunityService.applyToOpportunity(opportunityId, request);
    }

    @GetMapping("/{opportunityId}/applications")
    public List<OpportunityApplicationResponse> getApplications(@PathVariable Long opportunityId) {
        return opportunityService.getApplications(opportunityId);
    }
}
