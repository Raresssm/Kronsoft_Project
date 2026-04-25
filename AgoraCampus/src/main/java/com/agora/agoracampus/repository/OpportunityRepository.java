package com.agora.agoracampus.repository;

import com.agora.agoracampus.models.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {
}
