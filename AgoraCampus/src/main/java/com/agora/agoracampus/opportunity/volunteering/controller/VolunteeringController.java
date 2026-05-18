package com.agora.agoracampus.opportunity.volunteering.controller;

import com.agora.agoracampus.opportunity.volunteering.dto.response.VolunteeringResponse;
import com.agora.agoracampus.opportunity.volunteering.service.VolunteeringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opportunities/{opportunityId}/volunteering")
public class VolunteeringController {

    private final VolunteeringService volunteeringService;

    public VolunteeringController(VolunteeringService volunteeringService) {
        this.volunteeringService = volunteeringService;
    }

    @GetMapping
    public VolunteeringResponse getVolunteeringByOpportunity(@PathVariable Long opportunityId) {
        return volunteeringService.getByOpportunityId(opportunityId);
    }
}
