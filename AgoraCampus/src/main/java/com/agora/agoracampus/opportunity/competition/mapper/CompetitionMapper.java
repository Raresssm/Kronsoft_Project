package com.agora.agoracampus.opportunity.competition.mapper;

import com.agora.agoracampus.opportunity.competition.dto.request.CompetitionDetailsRequest;
import com.agora.agoracampus.opportunity.competition.dto.response.CompetitionResponse;
import com.agora.agoracampus.opportunity.competition.model.Competition;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import org.springframework.stereotype.Component;

@Component
public class CompetitionMapper {

    public Competition toEntity(CompetitionDetailsRequest details, Opportunity opportunity) {
        return Competition.builder()
                .opportunity(opportunity)
                .theme(details.theme())
                .eligibility(details.eligibility())
                .prize(details.prize())
                .deadline(details.deadline())
                .build();
    }

    public CompetitionResponse toResponse(Competition competition) {
        if (competition == null) {
            return null;
        }
        return CompetitionResponse.builder()
                .competitionId(competition.getId())
                .opportunityId(competition.getOpportunity().getId())
                .theme(competition.getTheme())
                .eligibility(competition.getEligibility())
                .prize(competition.getPrize())
                .deadline(competition.getDeadline())
                .build();
    }
}
