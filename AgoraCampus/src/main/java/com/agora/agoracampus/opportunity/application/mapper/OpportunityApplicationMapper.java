package com.agora.agoracampus.opportunity.application.mapper;

import com.agora.agoracampus.opportunity.application.dto.response.OpportunityApplicationResponse;
import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import org.springframework.stereotype.Component;

@Component
public class OpportunityApplicationMapper {

    public OpportunityApplicationResponse toResponse(OpportunityApplication application) {
        return new OpportunityApplicationResponse(
                application.getId(),
                application.getOpportunity().getId(),
                application.getApplicantUser().getId(),
                application.getApplicantUser().getUsername(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }
}
