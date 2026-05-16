package com.agora.agoracampus.opportunity.competition.service;

import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.competition.dto.response.CompetitionResponse;
import com.agora.agoracampus.opportunity.competition.mapper.CompetitionMapper;
import com.agora.agoracampus.opportunity.competition.model.Competition;
import com.agora.agoracampus.opportunity.competition.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionMapper competitionMapper;

    public List<CompetitionResponse> findAll() {
        return competitionRepository.findAll()
                .stream()
                .map(competitionMapper::toResponse)
                .toList();
    }

    public CompetitionResponse findById(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Competition not found with the id:"+id));
        return competitionMapper.toResponse(competition);
    }

    public CompetitionResponse findByOpportunityId(Long opportunityId) {
        Competition competition = competitionRepository.findByOpportunityId(opportunityId)
                .orElseThrow(() -> new NotFoundException(
                        "Competition for opportunity " + opportunityId + " was not found."
                ));
        return competitionMapper.toResponse(competition);
    }

    public List<CompetitionResponse> findExpired() {
        return competitionRepository.findByDeadlineBefore(LocalDate.now())
                .stream()
                .map(competitionMapper::toResponse)
                .toList();
    }
}
