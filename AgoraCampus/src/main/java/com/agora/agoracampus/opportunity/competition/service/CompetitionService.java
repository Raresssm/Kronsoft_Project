package com.agora.agoracampus.opportunity.competition.service;

import com.agora.agoracampus.exception.CompetitionNotFoundException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.competition.dto.request.CreateCompetitionRequest;
import com.agora.agoracampus.opportunity.competition.dto.request.UpdateCompetitionRequest;
import com.agora.agoracampus.opportunity.competition.dto.response.CompetitionResponse;
import com.agora.agoracampus.opportunity.competition.mapper.CompetitionMapper;
import com.agora.agoracampus.opportunity.competition.model.Competition;
import com.agora.agoracampus.opportunity.competition.repository.CompetitionRepository;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.core.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final OpportunityRepository opportunityRepository;
    private final CompetitionMapper competitionMapper;

    public List<CompetitionResponse> findAll() {
        return competitionRepository.findAll()
                .stream()
                .map(competitionMapper::toResponse)
                .toList();
    }

    public CompetitionResponse findById(Long id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
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

    public CompetitionResponse create(CreateCompetitionRequest request) {
        Opportunity opportunity = opportunityRepository.findById(request.getOpportunityId())
                .orElseThrow(() -> new NotFoundException(
                        "Opportunity " + request.getOpportunityId() + " was not found."
                ));
        Competition competition = competitionMapper.toEntity(request, opportunity);
        Competition saved = competitionRepository.save(competition);
        return competitionMapper.toResponse(saved);
    }

    public CompetitionResponse update(Long id, UpdateCompetitionRequest request) {
        Competition existing = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
        competitionMapper.updateEntity(existing, request);
        Competition updated = competitionRepository.save(existing);
        return competitionMapper.toResponse(updated);
    }

    public void deleteById(Long id) {
        if (!competitionRepository.existsById(id)) {
            throw new CompetitionNotFoundException(id);
        }
        competitionRepository.deleteById(id);
    }
}
