package com.agora.agoracampus.mappers;

import org.springframework.stereotype.Component;
import com.agora.agoracampus.dto.request.CreateCompetitionRequest;
import com.agora.agoracampus.dto.request.UpdateCompetitionRequest;
import com.agora.agoracampus.dto.response.CompetitionResponse;
import com.agora.agoracampus.models.Competition;

/**
 * Mapper pentru Competition.
 * Responsabil exclusiv pentru conversia intre tipuri:
 *   toEntity()      - CreateCompetitionRequest -> Competition (entitate noua)
 *   updateEntity()  - aplica UpdateCompetitionRequest pe entitate existenta
 *   toResponse()    - Competition -> CompetitionResponse (DTO de iesire)
 */
@Component
public class CompetitionMapper {

    public Competition toEntity(CreateCompetitionRequest request) {
        return Competition.builder()
                .opportunityId(request.getOpportunityId())
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
                .competitionId(competition.getCompetitionId())
                .opportunityId(competition.getOpportunityId())
                .theme(competition.getTheme())
                .eligibility(competition.getEligibility())
                .prize(competition.getPrize())
                .deadline(competition.getDeadline())
                .build();
    }
}
