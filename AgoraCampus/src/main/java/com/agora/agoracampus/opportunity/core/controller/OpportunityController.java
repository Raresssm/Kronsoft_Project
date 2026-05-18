package com.agora.agoracampus.opportunity.core.controller;

import com.agora.agoracampus.opportunity.application.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.opportunity.application.dto.request.UpdateOpportunityApplicationStatusRequest;
import com.agora.agoracampus.opportunity.application.dto.response.OpportunityApplicationResponse;
import com.agora.agoracampus.opportunity.core.dto.request.CreateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.dto.request.UpdateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.dto.response.OpportunityResponse;
import com.agora.agoracampus.opportunity.core.model.OpportunityType;
import com.agora.agoracampus.opportunity.core.service.OpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    @PostMapping
    public ResponseEntity<OpportunityResponse> createOpportunity(
            @Valid @RequestBody CreateOpportunityRequest request,
            @RequestParam Long actingUserId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opportunityService.createOpportunity(request, actingUserId));
    }

    @GetMapping
    public ResponseEntity<List<OpportunityResponse>> listOpportunities(
            @RequestParam(required = false) OpportunityType type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long postedByUserId,
            @RequestParam Long actingUserId
    ) {
        return ResponseEntity.ok(opportunityService.listOpportunities(type, location, postedByUserId, actingUserId));
    }

    @GetMapping("/{opportunityId}")
    public ResponseEntity<OpportunityResponse> getOpportunity(
            @PathVariable Long opportunityId,
            @RequestParam Long actingUserId
    ) {
        return ResponseEntity.ok(opportunityService.getOpportunity(opportunityId, actingUserId));
    }

    @PutMapping("/{opportunityId}")
    public ResponseEntity<OpportunityResponse> updateOpportunity(
            @PathVariable Long opportunityId,
            @Valid @RequestBody UpdateOpportunityRequest request,
            @RequestParam Long actingUserId
    ) {
        return ResponseEntity.ok(opportunityService.updateOpportunity(opportunityId, actingUserId, request));
    }

    @DeleteMapping("/{opportunityId}")
    public ResponseEntity<Void> deleteOpportunity(
            @PathVariable Long opportunityId,
            @RequestParam Long actingUserId
    ) {
        opportunityService.deleteOpportunity(opportunityId, actingUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{opportunityId}/applications")
    public ResponseEntity<OpportunityApplicationResponse> applyToOpportunity(
            @PathVariable Long opportunityId,
            @Valid @RequestBody CreateOpportunityApplicationRequest request,
            @RequestParam Long actingUserId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opportunityService.applyToOpportunity(opportunityId, request, actingUserId));
    }

    @GetMapping("/{opportunityId}/applications")
    public ResponseEntity<List<OpportunityApplicationResponse>> getApplications(
            @PathVariable Long opportunityId,
            @RequestParam Long actingUserId
    ) {
        return ResponseEntity.ok(opportunityService.getApplications(opportunityId, actingUserId));
    }

    @GetMapping("/applications/users/{applicantUserId}")
    public ResponseEntity<List<OpportunityApplicationResponse>> getApplicationsByApplicant(
            @PathVariable Long applicantUserId,
            @RequestParam Long actingUserId
    ) {
        return ResponseEntity.ok(opportunityService.getApplicationsByApplicant(applicantUserId, actingUserId));
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<OpportunityApplicationResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateOpportunityApplicationStatusRequest request,
            @RequestParam Long actingUserId
    ) {
        return ResponseEntity.ok(opportunityService.updateApplicationStatus(applicationId, actingUserId, request));
    }

    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable Long applicationId,
            @RequestParam Long actingUserId
    ) {
        opportunityService.deleteApplication(applicationId, actingUserId);
        return ResponseEntity.noContent().build();
    }
}
