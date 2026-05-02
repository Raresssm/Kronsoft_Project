package com.agora.agoracampus.opportunity.volunteering.mapper;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.volunteering.dto.request.VolunteeringDetailsRequest;
import com.agora.agoracampus.opportunity.volunteering.dto.response.VolunteeringResponse;
import com.agora.agoracampus.opportunity.volunteering.model.Volunteering;
import org.springframework.stereotype.Component;

@Component
public class VolunteeringMapper {

    public Volunteering toEntity(VolunteeringDetailsRequest detailsRequest, Opportunity opportunity) {
        Volunteering volunteering = new Volunteering();
        volunteering.setOpportunity(opportunity);
        volunteering.setCause(detailsRequest.cause());
        volunteering.setSchedule(detailsRequest.schedule());
        volunteering.setBenefits(detailsRequest.benefits());
        return volunteering;
    }

    public VolunteeringResponse toResponse(Volunteering volunteering) {
        if (volunteering == null) {
            return null;
        }

        return new VolunteeringResponse(
                volunteering.getId(),
                volunteering.getCause(),
                volunteering.getSchedule(),
                volunteering.getBenefits()
        );
    }
}
