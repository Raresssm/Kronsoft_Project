package com.agora.agoracampus.opportunity.volunteering.mapper;

import com.agora.agoracampus.opportunity.volunteering.dto.response.VolunteeringResponse;
import com.agora.agoracampus.opportunity.volunteering.model.Volunteering;
import org.springframework.stereotype.Component;

@Component
public class VolunteeringMapper {

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
