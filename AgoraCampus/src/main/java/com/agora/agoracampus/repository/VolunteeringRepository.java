package com.agora.agoracampus.repository;

import com.agora.agoracampus.domain.Volunteering;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteeringRepository extends JpaRepository<Volunteering, Long> {

    Optional<Volunteering> findByOpportunityId(Long opportunityId);
}
