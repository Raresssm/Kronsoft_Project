package com.agora.agoracampus.service;

import com.agora.agoracampus.models.*;
import com.agora.agoracampus.dto.response.*;
import com.agora.agoracampus.dto.request.*;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.repository.IndividualProfileRepository;
import com.agora.agoracampus.repository.OpportunityApplicationRepository;
import com.agora.agoracampus.repository.OpportunityRepository;
import com.agora.agoracampus.repository.OrganizationProfileRepository;
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

    public OpportunityService(
            AppUserService appUserService,
            OrganizationProfileRepository organizationProfileRepository,
            IndividualProfileRepository individualProfileRepository,
            OpportunityRepository opportunityRepository,
            OpportunityApplicationRepository opportunityApplicationRepository
    ) {
        this.appUserService = appUserService;
        this.organizationProfileRepository = organizationProfileRepository;
        this.individualProfileRepository = individualProfileRepository;
        this.opportunityRepository = opportunityRepository;
        this.opportunityApplicationRepository = opportunityApplicationRepository;
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

        Opportunity opportunity = new Opportunity();
        opportunity.setPostedByUser(postedByUser);
        opportunity.setOrganizationProfile(postingProfile.organizationProfile());
        opportunity.setIndividualProfile(postingProfile.individualProfile());
        opportunity.setType(request.type());
        opportunity.setTitle(request.title());
        opportunity.setLocation(request.location());
        opportunity.setPeriod(request.period());
        opportunity.setDescription(request.description());
        opportunity.setAdditionalInfo(request.additionalInfo());

        if (request.volunteering() != null) {
            Volunteering volunteering = new Volunteering();
            volunteering.setCause(request.volunteering().cause());
            volunteering.setSchedule(request.volunteering().schedule());
            volunteering.setBenefits(request.volunteering().benefits());
            volunteering.setOpportunity(opportunity);
            opportunity.setVolunteering(volunteering);
        }

        return toResponse(opportunityRepository.save(opportunity));
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

        return toResponse(opportunityApplicationRepository.save(application));
    }

    @Transactional
    public List<OpportunityApplicationResponse> getApplications(Long opportunityId) {
        getRequiredOpportunityEntity(opportunityId);
        return opportunityApplicationRepository.findByOpportunityIdOrderByAppliedAtDesc(opportunityId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OpportunityResponse getOpportunity(Long opportunityId) {
        return toResponse(getRequiredOpportunityEntity(opportunityId));
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
                .map(this::toResponse)
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

    private OpportunityResponse toResponse(Opportunity opportunity) {
        return new OpportunityResponse(
                opportunity.getId(),
                opportunity.getPostedByUser().getId(),
                opportunity.getType(),
                opportunity.getTitle(),
                opportunity.getLocation(),
                opportunity.getPeriod(),
                opportunity.getDescription(),
                opportunity.getAdditionalInfo(),
                opportunity.getCreatedAt(),
                toPostingProfileResponse(opportunity),
                toVolunteeringResponse(opportunity.getVolunteering())
        );
    }

    private PostingProfileResponse toPostingProfileResponse(Opportunity opportunity) {
        if (opportunity.getOrganizationProfile() != null) {
            OrganizationProfile organizationProfile = opportunity.getOrganizationProfile();
            return new PostingProfileResponse(
                    ProfileType.ORGANIZATION,
                    (long)organizationProfile.getId(),
                    null,
                    (long)organizationProfile.getProfile().getId(),
                    organizationProfile.getProfile().getAppUser().getId(),
                    organizationProfile.getOrganizationName()
            );
        }

        IndividualProfile individualProfile = opportunity.getIndividualProfile();
        return new PostingProfileResponse(
                ProfileType.INDIVIDUAL,
                null,
                (long)individualProfile.getId(),
                (long)individualProfile.getProfile().getId(),
                individualProfile.getProfile().getAppUser().getId(),
                individualProfile.getFirstName() + " " + individualProfile.getLastName()
        );
    }

    private VolunteeringResponse toVolunteeringResponse(Volunteering volunteering) {
        if (volunteering == null) {
            return null;
        }
        return new VolunteeringResponse(
                volunteering.getId(),
                volunteering.getCause(),
                volunteering.getSchedule(),
                volunteering.getBenefits()
        );
    }

    private OpportunityApplicationResponse toResponse(OpportunityApplication application) {
        return new OpportunityApplicationResponse(
                application.getId(),
                application.getOpportunity().getId(),
                application.getApplicantUser().getId(),
                application.getApplicantUser().getUsername(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }

    private record PostingProfileSelection(
            OrganizationProfile organizationProfile,
            IndividualProfile individualProfile
    ) {
    }
}
