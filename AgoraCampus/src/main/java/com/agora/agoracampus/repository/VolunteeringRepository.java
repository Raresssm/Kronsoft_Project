package com.agora.agoracampus.repository;

import com.agora.agoracampus.models.Volunteering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VolunteeringRepository extends JpaRepository<Volunteering, Long> {

    Optional<Volunteering> findByOpportunityId(Long opportunityId);
}
