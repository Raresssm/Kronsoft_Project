package com.agora.agoracampus.opportunity.volunteering.repository;

import com.agora.agoracampus.opportunity.volunteering.model.Volunteering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VolunteeringRepository extends JpaRepository<Volunteering, Long> {

    Optional<Volunteering> findByOpportunityId(Long opportunityId);
}
