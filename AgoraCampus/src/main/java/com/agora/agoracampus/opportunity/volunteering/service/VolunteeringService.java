package com.agora.agoracampus.opportunity.volunteering.service;

import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.volunteering.dto.response.VolunteeringResponse;
import com.agora.agoracampus.opportunity.volunteering.mapper.VolunteeringMapper;
import com.agora.agoracampus.opportunity.volunteering.model.Volunteering;
import com.agora.agoracampus.opportunity.volunteering.repository.VolunteeringRepository;
import org.springframework.stereotype.Service;

@Service
public class VolunteeringService {

    private final VolunteeringRepository volunteeringRepository;
    private final VolunteeringMapper volunteeringMapper;

    public VolunteeringService(
            VolunteeringRepository volunteeringRepository,
            VolunteeringMapper volunteeringMapper
    ) {
        this.volunteeringRepository = volunteeringRepository;
        this.volunteeringMapper = volunteeringMapper;
    }

    public VolunteeringResponse getByOpportunityId(Long opportunityId) {
        Volunteering volunteering = volunteeringRepository.findByOpportunityId(opportunityId)
                .orElseThrow(() -> new NotFoundException(
                        "Volunteering details for opportunity " + opportunityId + " were not found."
                ));

        return volunteeringMapper.toResponse(volunteering);
    }
}
