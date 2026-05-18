package com.agora.agoracampus.opportunity.application.repository;

import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpportunityApplicationRepository extends JpaRepository<OpportunityApplication, Long> {

    boolean existsByOpportunityIdAndApplicantUserId(Long opportunityId, Long applicantUserId);

    Optional<OpportunityApplication> findByOpportunityIdAndApplicantUserId(Long opportunityId, Long applicantUserId);

    List<OpportunityApplication> findByOpportunityIdOrderByAppliedAtDesc(Long opportunityId);

    List<OpportunityApplication> findByApplicantUser_IdOrderByAppliedAtDesc(Long applicantUserId);

    void deleteByOpportunityId(Long opportunityId);
}
