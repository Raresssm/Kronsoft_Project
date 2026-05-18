package com.agora.agoracampus.opportunity.application.service;

import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.application.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.opportunity.application.dto.response.OpportunityApplicationResponse;
import com.agora.agoracampus.opportunity.application.mapper.OpportunityApplicationMapper;
import com.agora.agoracampus.opportunity.application.model.ApplicationStatus;
import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import com.agora.agoracampus.opportunity.application.repository.OpportunityApplicationRepository;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.core.repository.OpportunityRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpportunityApplicationService {

    private final OpportunityApplicationRepository opportunityApplicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final AppUserService appUserService;
    private final OpportunityApplicationMapper opportunityApplicationMapper;

    public List<OpportunityApplicationResponse> findAll() {
        return opportunityApplicationRepository.findAll()
                .stream()
                .map(opportunityApplicationMapper::toResponse)
                .toList();
    }

    public OpportunityApplicationResponse findById(Long id) {
        OpportunityApplication application = opportunityApplicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Opportunity application not found with the id:" + id));
        return opportunityApplicationMapper.toResponse(application);
    }

    public List<OpportunityApplicationResponse> findByOpportunityId(Long opportunityId) {
        return opportunityApplicationRepository.findByOpportunityIdOrderByAppliedAtDesc(opportunityId)
                .stream()
                .map(opportunityApplicationMapper::toResponse)
                .toList();
    }

    public OpportunityApplicationResponse create(Long opportunityId, CreateOpportunityApplicationRequest request) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NotFoundException("Opportunity " + opportunityId + " was not found."));
        AppUser applicant = appUserService.getRequiredEntity(request.applicantUserId());

        if (opportunityApplicationRepository.existsByOpportunityIdAndApplicantUserId(opportunityId, applicant.getId())) {
            throw new ConflictException("The user has already applied to this opportunity.");
        }

        OpportunityApplication application = new OpportunityApplication();
        application.setOpportunity(opportunity);
        application.setApplicantUser(applicant);
        application.setStatus(ApplicationStatus.PENDING);

        return opportunityApplicationMapper.toResponse(opportunityApplicationRepository.save(application));
    }

    public void deleteById(Long id) {
        if (!opportunityApplicationRepository.existsById(id)) {
            throw new NotFoundException("Opportunity application not found with the id:" + id);
        }
        opportunityApplicationRepository.deleteById(id);
    }
}
