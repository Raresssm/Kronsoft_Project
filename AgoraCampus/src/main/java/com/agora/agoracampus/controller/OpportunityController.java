package com.agora.agoracampus.controller;

import com.agora.agoracampus.domain.OpportunityType;
import com.agora.agoracampus.dto.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.dto.CreateOpportunityRequest;
import com.agora.agoracampus.dto.OpportunityApplicationResponse;
import com.agora.agoracampus.dto.OpportunityResponse;
import com.agora.agoracampus.service.OpportunityService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
