package com.agora.agoracampus.opportunity.core.service;

import com.agora.agoracampus.opportunity.application.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.opportunity.application.dto.response.OpportunityApplicationResponse;
import com.agora.agoracampus.opportunity.application.mapper.OpportunityApplicationMapper;
import com.agora.agoracampus.opportunity.application.model.ApplicationStatus;
import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import com.agora.agoracampus.opportunity.core.dto.request.CreateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.dto.response.OpportunityResponse;
import com.agora.agoracampus.opportunity.core.mapper.OpportunityMapper;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.core.model.OpportunityType;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.opportunity.volunteering.dto.request.VolunteeringDetailsRequest;
import com.agora.agoracampus.opportunity.volunteering.mapper.VolunteeringMapper;
import com.agora.agoracampus.opportunity.volunteering.model.Volunteering;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import com.agora.agoracampus.opportunity.application.repository.OpportunityApplicationRepository;
import com.agora.agoracampus.opportunity.core.repository.OpportunityRepository;
import com.agora.agoracampus.profile.organization.repository.OrganizationProfileRepository;
import com.agora.agoracampus.user.core.service.AppUserService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpportunityService {

    private final AppUserService appUserService;
    private final OrganizationProfileRepository organizationProfileRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityApplicationRepository opportunityApplicationRepository;
    private final OpportunityMapper opportunityMapper;
    private final OpportunityApplicationMapper opportunityApplicationMapper;
    private final VolunteeringMapper volunteeringMapper;

    public OpportunityService(
            AppUserService appUserService,
            OrganizationProfileRepository organizationProfileRepository,
            IndividualProfileRepository individualProfileRepository,
            OpportunityRepository opportunityRepository,
            OpportunityApplicationRepository opportunityApplicationRepository,
            OpportunityMapper opportunityMapper,
            OpportunityApplicationMapper opportunityApplicationMapper,
            VolunteeringMapper volunteeringMapper
    ) {
        this.appUserService = appUserService;
        this.organizationProfileRepository = organizationProfileRepository;
        this.individualProfileRepository = individualProfileRepository;
        this.opportunityRepository = opportunityRepository;
        this.opportunityApplicationRepository = opportunityApplicationRepository;
        this.opportunityMapper = opportunityMapper;
        this.opportunityApplicationMapper = opportunityApplicationMapper;
        this.volunteeringMapper = volunteeringMapper;
    }

    @Transactional
    public OpportunityResponse createOpportunity(CreateOpportunityRequest request) {
        AppUser postedByUser = appUserService.getRequiredEntity(request.postedByUserId());
        PostingProfileSelection postingProfile = resolvePostingProfile(
                postedByUser,
                request.organizationProfileId(),
                request.individualProfileId()
        );

        validateOpportunityDetails(request.type(), request.volunteering());

        Opportunity opportunity = opportunityMapper.toEntity(
                request,
                postedByUser,
                postingProfile.organizationProfile(),
                postingProfile.individualProfile()
        );

        if (request.volunteering() != null) {
            Volunteering volunteering = volunteeringMapper.toEntity(request.volunteering(), opportunity);
            opportunity.setVolunteering(volunteering);
        }

        return opportunityMapper.toResponse(opportunityRepository.save(opportunity));
    }

    @Transactional
    public OpportunityApplicationResponse applyToOpportunity(Long opportunityId, CreateOpportunityApplicationRequest request) {
        Opportunity opportunity = getRequiredOpportunityEntity(opportunityId);
        AppUser applicant = appUserService.getRequiredEntity(request.applicantUserId());

        if (opportunity.getPostedByUser().getId().equals(applicant.getId())) {
            throw new BadRequestException("Users cannot apply to their own opportunities.");
        }
        if (opportunityApplicationRepository.existsByOpportunityIdAndApplicantUserId(opportunityId, applicant.getId())) {
            throw new ConflictException("The user has already applied to this opportunity.");
        }

        OpportunityApplication application = new OpportunityApplication();
        application.setOpportunity(opportunity);
        application.setApplicantUser(applicant);
        application.setStatus(ApplicationStatus.PENDING);

        return opportunityApplicationMapper.toResponse(opportunityApplicationRepository.save(application));
    }

    @Transactional
    public List<OpportunityApplicationResponse> getApplications(Long opportunityId) {
        getRequiredOpportunityEntity(opportunityId);
        return opportunityApplicationRepository.findByOpportunityIdOrderByAppliedAtDesc(opportunityId)
                .stream()
                .map(opportunityApplicationMapper::toResponse)
                .toList();
    }

    @Transactional
    public OpportunityResponse getOpportunity(Long opportunityId) {
        return opportunityMapper.toResponse(getRequiredOpportunityEntity(opportunityId));
    }

    @Transactional
    public List<OpportunityResponse> listOpportunities(OpportunityType type, String location, Long postedByUserId) {
        Specification<Opportunity> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (type != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("type"), type));
        }
        if (location != null && !location.isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
        }
        if (postedByUserId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("postedByUser").get("id"), postedByUserId));
        }

        return opportunityRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(opportunityMapper::toResponse)
                .toList();
    }

    private Opportunity getRequiredOpportunityEntity(Long opportunityId) {
        return opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new NotFoundException("Opportunity " + opportunityId + " was not found."));
    }

    private PostingProfileSelection resolvePostingProfile(
            AppUser postedByUser,
            Long organizationProfileId,
            Long individualProfileId
    ) {
        if ((organizationProfileId == null) == (individualProfileId == null)) {
            throw new BadRequestException("Exactly one posting profile ID must be provided.");
        }

        if (organizationProfileId != null) {
            OrganizationProfile organizationProfile = organizationProfileRepository.findById(organizationProfileId)
                    .orElseThrow(() -> new NotFoundException(
                            "Organization profile " + organizationProfileId + " was not found."
                    ));
            if (!organizationProfile.getProfile().getAppUser().getId().equals(postedByUser.getId())) {
                throw new BadRequestException("The organization profile does not belong to the posting user.");
            }
            return new PostingProfileSelection(organizationProfile, null);
        }

        IndividualProfile individualProfile = individualProfileRepository.findById(individualProfileId)
                .orElseThrow(() -> new NotFoundException(
                        "Individual profile " + individualProfileId + " was not found."
                ));
        if (!individualProfile.getProfile().getAppUser().getId().equals(postedByUser.getId())) {
            throw new BadRequestException("The individual profile does not belong to the posting user.");
        }
        return new PostingProfileSelection(null, individualProfile);
    }

    private void validateOpportunityDetails(OpportunityType type, VolunteeringDetailsRequest volunteeringDetails) {
        if (type == OpportunityType.VOLUNTEERING && volunteeringDetails == null) {
            throw new BadRequestException("Volunteering opportunities require volunteering details.");
        }
        if (type != OpportunityType.VOLUNTEERING && volunteeringDetails != null) {
            throw new BadRequestException(
                    "Volunteering details can only be provided when the opportunity type is VOLUNTEERING."
            );
        }
    }

    private record PostingProfileSelection(
            OrganizationProfile organizationProfile,
            IndividualProfile individualProfile
    ) {
    }
}
