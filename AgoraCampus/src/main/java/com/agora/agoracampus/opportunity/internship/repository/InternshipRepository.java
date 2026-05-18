package com.agora.agoracampus.opportunity.internship.repository;

import com.agora.agoracampus.opportunity.internship.model.Internship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InternshipRepository extends JpaRepository<Internship, Long> {

    Optional<Internship> findByOpportunityId(Long opportunityId);
}
