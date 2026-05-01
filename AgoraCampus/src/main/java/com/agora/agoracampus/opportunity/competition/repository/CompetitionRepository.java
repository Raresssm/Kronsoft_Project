package com.agora.agoracampus.opportunity.competition.repository;

import com.agora.agoracampus.opportunity.competition.model.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    Optional<Competition> findByOpportunityId(Long opportunityId);

    List<Competition> findByDeadlineBefore(LocalDate date);
}
