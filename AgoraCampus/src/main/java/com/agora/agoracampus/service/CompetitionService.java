package com.agora.agoracampus.service;

import com.agora.agoracampus.dto.request.CreateCompetitionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.agora.agoracampus.dto.request.UpdateCompetitionRequest;
import com.agora.agoracampus.dto.response.CompetitionResponse;
import com.agora.agoracampus.exception.CompetitionNotFoundException;
import com.agora.agoracampus.mappers.CompetitionMapper;
import com.agora.agoracampus.models.Competition;
import com.agora.agoracampus.repository.CompetitionRepository;

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

    public CompetitionResponse findById(Integer id) {
        Competition competition = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
        return competitionMapper.toResponse(competition);
    }

    public List<CompetitionResponse> findByOpportunityId(Integer opportunityId) {
        return competitionRepository.findByOpportunityId(opportunityId)
                .stream()
                .map(competitionMapper::toResponse)
                .toList();
    }

    // Competitii cu deadline-ul expirat (utile pentru curatare / rapoarte)
    public List<CompetitionResponse> findExpired() {
        return competitionRepository.findByDeadlineBefore(LocalDate.now())
                .stream()
                .map(competitionMapper::toResponse)
                .toList();
    }

    public CompetitionResponse create(CreateCompetitionRequest request) {
        Competition competition = competitionMapper.toEntity(request);
        Competition saved = competitionRepository.save(competition);
        return competitionMapper.toResponse(saved);
    }

    public CompetitionResponse update(Integer id, UpdateCompetitionRequest request) {
        Competition existing = competitionRepository.findById(id)
                .orElseThrow(() -> new CompetitionNotFoundException(id));
        competitionMapper.updateEntity(existing, request);
        Competition updated = competitionRepository.save(existing);
        return competitionMapper.toResponse(updated);
    }

    public void deleteById(Integer id) {
        if (!competitionRepository.existsById(id)) {
            throw new CompetitionNotFoundException(id);
        }
        competitionRepository.deleteById(id);
    }
}
