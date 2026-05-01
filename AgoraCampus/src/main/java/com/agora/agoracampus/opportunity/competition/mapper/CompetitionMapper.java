package com.agora.agoracampus.opportunity.competition.mapper;

import com.agora.agoracampus.opportunity.competition.dto.request.CreateCompetitionRequest;
import com.agora.agoracampus.opportunity.competition.dto.request.UpdateCompetitionRequest;
import com.agora.agoracampus.opportunity.competition.dto.response.CompetitionResponse;
import com.agora.agoracampus.opportunity.competition.model.Competition;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import org.springframework.stereotype.Component;

@Component
public class CompetitionMapper {

    public Competition toEntity(CreateCompetitionRequest request, Opportunity opportunity) {
        return Competition.builder()
                .opportunity(opportunity)
                .theme(request.getTheme())
                .eligibility(request.getEligibility())
                .prize(request.getPrize())
                .deadline(request.getDeadline())
                .build();
    }

    public void updateEntity(Competition competition, UpdateCompetitionRequest request) {
        competition.setTheme(request.getTheme());
        competition.setEligibility(request.getEligibility());
        competition.setPrize(request.getPrize());
        competition.setDeadline(request.getDeadline());
    }

    public CompetitionResponse toResponse(Competition competition) {
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
