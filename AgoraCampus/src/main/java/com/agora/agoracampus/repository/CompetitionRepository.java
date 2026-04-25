package com.agora.agoracampus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agora.agoracampus.models.Competition;

import java.time.LocalDate;
import java.util.List;

public interface CompetitionRepository extends JpaRepository<Competition, Integer> {


    List<Competition> findByOpportunityId(Integer opportunityId);


    List<Competition> findByDeadlineBefore(LocalDate date);
}
