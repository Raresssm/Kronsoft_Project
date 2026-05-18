package com.agora.agoracampus.opportunity.core.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.application.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.opportunity.application.dto.request.UpdateOpportunityApplicationStatusRequest;
import com.agora.agoracampus.opportunity.application.dto.response.OpportunityApplicationResponse;
import com.agora.agoracampus.opportunity.application.mapper.OpportunityApplicationMapper;
import com.agora.agoracampus.opportunity.application.model.ApplicationStatus;
import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import com.agora.agoracampus.opportunity.application.repository.OpportunityApplicationRepository;
import com.agora.agoracampus.opportunity.core.dto.request.CreateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.dto.request.UpdateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.dto.response.OpportunityResponse;
import com.agora.agoracampus.opportunity.core.mapper.OpportunityMapper;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.core.model.OpportunityActorRole;
import com.agora.agoracampus.opportunity.core.model.OpportunityType;
import com.agora.agoracampus.opportunity.competition.dto.request.CompetitionDetailsRequest;
import com.agora.agoracampus.opportunity.competition.mapper.CompetitionMapper;
import com.agora.agoracampus.opportunity.core.repository.OpportunityRepository;
import com.agora.agoracampus.opportunity.internship.dto.request.InternshipDetailsRequest;
import com.agora.agoracampus.opportunity.internship.mapper.InternshipMapper;
import com.agora.agoracampus.opportunity.studentproject.dto.request.StudentProjectDetailsRequest;
import com.agora.agoracampus.opportunity.studentproject.mapper.StudentProjectMapper;
import com.agora.agoracampus.opportunity.volunteering.dto.request.VolunteeringDetailsRequest;
import com.agora.agoracampus.opportunity.volunteering.mapper.VolunteeringMapper;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.profile.organization.repository.OrganizationProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import com.agora.agoracampus.security.SecurityAuthorityUtils;
import com.agora.agoracampus.user.core.service.AppUserService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpportunityService {

    private final AppUserService appUserService;
    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;
    private final OrganizationProfileRepository organizationProfileRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityApplicationRepository opportunityApplicationRepository;
    private final OpportunityMapper opportunityMapper;
    private final OpportunityApplicationMapper opportunityApplicationMapper;
    private final VolunteeringMapper volunteeringMapper;
    private final CompetitionMapper competitionMapper;
    private final InternshipMapper internshipMapper;
    private final StudentProjectMapper studentProjectMapper;

    public OpportunityService(
            AppUserService appUserService,
            AppUserRepository appUserRepository,
            ProfileRepository profileRepository,
            OrganizationProfileRepository organizationProfileRepository,
            IndividualProfileRepository individualProfileRepository,
            OpportunityRepository opportunityRepository,
            OpportunityApplicationRepository opportunityApplicationRepository,
            OpportunityMapper opportunityMapper,
            OpportunityApplicationMapper opportunityApplicationMapper,
            VolunteeringMapper volunteeringMapper,
            CompetitionMapper competitionMapper,
            InternshipMapper internshipMapper,
            StudentProjectMapper studentProjectMapper
    ) {
        this.appUserService = appUserService;
        this.appUserRepository = appUserRepository;
        this.profileRepository = profileRepository;
        this.organizationProfileRepository = organizationProfileRepository;
        this.individualProfileRepository = individualProfileRepository;
        this.opportunityRepository = opportunityRepository;
        this.opportunityApplicationRepository = opportunityApplicationRepository;
        this.opportunityMapper = opportunityMapper;
        this.opportunityApplicationMapper = opportunityApplicationMapper;
        this.volunteeringMapper = volunteeringMapper;
        this.competitionMapper = competitionMapper;
        this.internshipMapper = internshipMapper;
        this.studentProjectMapper = studentProjectMapper;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public OpportunityResponse createOpportunity(CreateOpportunityRequest request, Long actingUserId) {
        OpportunityActorRole actorRole = resolveActorRole(actingUserId);

        if (actorRole == OpportunityActorRole.INDIVIDUAL
                && request.type() != OpportunityType.STUDENT_PROJECT
                && request.type() != OpportunityType.COMPETITION) {
            throw new BadRequestException("Individual users can create only student project or competition opportunities.");
        }

        AppUser postedByUser = appUserService.getRequiredEntity(request.postedByUserId());
        PostingProfileSelection postingProfile = resolvePostingProfile(
                postedByUser,
                request.organizationProfileId(),
                request.individualProfileId()
        );

        validateOpportunityDetails(request);

        Opportunity opportunity = opportunityMapper.toEntity(
                request,
                postedByUser,
                postingProfile.organizationProfile(),
                postingProfile.individualProfile()
        );

        attachSubtypeDetails(request, opportunity);

        return opportunityMapper.toResponse(opportunityRepository.save(opportunity));
    }

    // ── APPLY ─────────────────────────────────────────────────────────────────

    @Transactional
    public OpportunityApplicationResponse applyToOpportunity(
            Long opportunityId,
            CreateOpportunityApplicationRequest request,
            Long actingUserId
    ) {
        resolveActorRole(actingUserId); // valideaza ca userul exista si e autentificat

        Opportunity opportunity = getRequiredOpportunityEntity(opportunityId);
        AppUser applicant = appUserService.getRequiredEntity(request.applicantUserId());

        // actingUserId trebuie sa fie acelasi cu applicantUserId
        if (!actingUserId.equals(applicant.getId())) {
            throw new BadRequestException("actingUserId must match the applicantUserId.");
        }
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

    // ── GET APPLICATIONS ──────────────────────────────────────────────────────

    @Transactional
    public List<OpportunityApplicationResponse> getApplications(Long opportunityId, Long actingUserId) {
        OpportunityActorRole actorRole = resolveActorRole(actingUserId);
        Opportunity opportunity = getRequiredOpportunityEntity(opportunityId);

        // OWNER (cel care a postat) sau ADMIN pot vedea aplicatiile
        requireAdminOrOwner(actorRole, actingUserId, opportunity.getPostedByUser().getId());

        return opportunityApplicationRepository.findByOpportunityIdOrderByAppliedAtDesc(opportunityId)
                .stream()
                .map(opportunityApplicationMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<OpportunityApplicationResponse> getApplicationsByApplicant(Long applicantUserId, Long actingUserId) {
        OpportunityActorRole actorRole = resolveActorRole(actingUserId);
        requireAdminOrOwner(actorRole, actingUserId, applicantUserId);

        return opportunityApplicationRepository.findByApplicantUser_IdOrderByAppliedAtDesc(applicantUserId)
                .stream()
                .map(opportunityApplicationMapper::toResponse)
                .toList();
    }

    @Transactional
    public OpportunityApplicationResponse updateApplicationStatus(
            Long applicationId,
            Long actingUserId,
            UpdateOpportunityApplicationStatusRequest request
    ) {
        OpportunityActorRole actorRole = resolveActorRole(actingUserId);
        OpportunityApplication application = opportunityApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Opportunity application " + applicationId + " was not found."));

        if (request.status() == ApplicationStatus.PENDING) {
            throw new BadRequestException("Cannot revert an application to PENDING.");
        }

        requireAdminOrOwner(actorRole, actingUserId, application.getOpportunity().getPostedByUser().getId());

        application.setStatus(request.status());
        return opportunityApplicationMapper.toResponse(opportunityApplicationRepository.save(application));
    }

    @Transactional
    public void deleteApplication(Long applicationId, Long actingUserId) {
        OpportunityActorRole actorRole = resolveActorRole(actingUserId);
        OpportunityApplication application = opportunityApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Opportunity application " + applicationId + " was not found."));

        boolean applicantOwnsApplication = actingUserId.equals(application.getApplicantUser().getId());
        boolean admin = actorRole == OpportunityActorRole.ADMIN;
        if (!admin && !applicantOwnsApplication) {
            throw new BadRequestException("Only the applicant or admins can delete this application.");
        }

        opportunityApplicationRepository.delete(application);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional
    public OpportunityResponse getOpportunity(Long opportunityId, Long actingUserId) {
        validateUserExists(actingUserId);
        return opportunityMapper.toResponse(getRequiredOpportunityEntity(opportunityId));
    }

    @Transactional
    public OpportunityResponse updateOpportunity(
            Long opportunityId,
            Long actingUserId,
            UpdateOpportunityRequest request
    ) {
        OpportunityActorRole actorRole = resolveActorRole(actingUserId);
        Opportunity opportunity = getRequiredOpportunityEntity(opportunityId);
        requireAdminOrOwner(actorRole, actingUserId, opportunity.getPostedByUser().getId());

        opportunity.setTitle(request.title());
        opportunity.setLocation(request.location());
        opportunity.setPeriod(request.period());
        opportunity.setDescription(request.description());
        opportunity.setAdditionalInfo(request.additionalInfo());

        return opportunityMapper.toResponse(opportunityRepository.save(opportunity));
    }

    @Transactional
    public void deleteOpportunity(Long opportunityId, Long actingUserId) {
        OpportunityActorRole actorRole = resolveActorRole(actingUserId);
        Opportunity opportunity = getRequiredOpportunityEntity(opportunityId);
        requireAdminOrOwner(actorRole, actingUserId, opportunity.getPostedByUser().getId());

        opportunityApplicationRepository.deleteByOpportunityId(opportunityId);
        opportunityRepository.delete(opportunity);
    }

    @Transactional
    public List<OpportunityResponse> listOpportunities(OpportunityType type, String location, Long postedByUserId, Long actingUserId) {
        validateUserExists(actingUserId);

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

    // ── PRIVATE ───────────────────────────────────────────────────────────────

    private void validateUserExists(Long userId) {
        appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " was not found."));
    }

    private OpportunityActorRole resolveActorRole(Long actingUserId) {
        validateUserExists(actingUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = SecurityAuthorityUtils.isAuthenticated(authentication);
        boolean isAdmin = authenticated && SecurityAuthorityUtils.hasAdminAuthority(authentication);

        if (authenticated) {
            validateAuthenticatedIdentity(actingUserId, isAdmin, authentication);
        }

        if (isAdmin) {
            return OpportunityActorRole.ADMIN;
        }

        Profile profile = profileRepository.findByAppUser_Id(actingUserId)
                .orElseThrow(() -> new BadRequestException(
                        "User " + actingUserId + " has no profile."
                ));

        if (profile.getOrganizationProfile() != null) {
            return OpportunityActorRole.ORGANIZATION;
        }
        if (profile.getIndividualProfile() != null) {
            return OpportunityActorRole.INDIVIDUAL; // INDIVIDUAL = poate vedea si aplica
        }

        throw new BadRequestException("User " + actingUserId + " has unsupported profile type.");
    }

    private void requireAdminOrOwner(
            OpportunityActorRole actorRole,
            Long actingUserId,
        Long ownerId
    ) {
        if (actorRole != OpportunityActorRole.ADMIN && !actingUserId.equals(ownerId)) {
            throw new BadRequestException("Only the owner or admins can perform this action.");
        }
    }

    private void validateAuthenticatedIdentity(
            Long actingUserId,
            boolean isAdmin,
            Authentication authentication
    ) {
        if (isAdmin) return;

        String principalName = authentication.getName();
        if (principalName == null || principalName.isBlank() || "anonymousUser".equals(principalName)) {
            return;
        }

        AppUser authenticatedUser = appUserRepository.findByKeycloakId(principalName)
                .orElseThrow(() -> new BadRequestException(
                        "Authenticated principal is not registered as an application user."
                ));

        if (!authenticatedUser.getId().equals(actingUserId)) {
            throw new BadRequestException("actingUserId must match the authenticated user.");
        }
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
                            "Organization profile " + organizationProfileId + " was not found."));
            if (!organizationProfile.getProfile().getAppUser().getId().equals(postedByUser.getId())) {
                throw new BadRequestException("The organization profile does not belong to the posting user.");
            }
            return new PostingProfileSelection(organizationProfile, null);
        }

        IndividualProfile individualProfile = individualProfileRepository.findById(individualProfileId)
                .orElseThrow(() -> new NotFoundException(
                        "Individual profile " + individualProfileId + " was not found."));
        if (!individualProfile.getProfile().getAppUser().getId().equals(postedByUser.getId())) {
            throw new BadRequestException("The individual profile does not belong to the posting user.");
        }
        return new PostingProfileSelection(null, individualProfile);
    }

    private void attachSubtypeDetails(CreateOpportunityRequest request, Opportunity opportunity) {
        switch (request.type()) {
            case VOLUNTEERING -> opportunity.setVolunteering(
                    volunteeringMapper.toEntity(requireVolunteeringDetails(request), opportunity));
            case COMPETITION -> opportunity.setCompetition(
                    competitionMapper.toEntity(requireCompetitionDetails(request), opportunity));
            case INTERNSHIP -> opportunity.setInternship(
                    internshipMapper.toEntity(requireInternshipDetails(request), opportunity));
            case STUDENT_PROJECT -> opportunity.setStudentProject(
                    studentProjectMapper.toEntity(requireStudentProjectDetails(request), opportunity));
        }
    }

    private void validateOpportunityDetails(CreateOpportunityRequest request) {
        switch (request.type()) {
            case VOLUNTEERING -> {
                requireVolunteeringDetails(request);
                if (request.competition() != null || request.internship() != null || request.studentProject() != null) {
                    throw new BadRequestException(
                            "Only volunteering details may be provided for VOLUNTEERING opportunities.");
                }
            }
            case COMPETITION -> {
                requireCompetitionDetails(request);
                if (request.volunteering() != null || request.internship() != null || request.studentProject() != null) {
                    throw new BadRequestException(
                            "Only competition details may be provided for COMPETITION opportunities.");
                }
            }
            case INTERNSHIP -> {
                requireInternshipDetails(request);
                if (request.volunteering() != null || request.competition() != null || request.studentProject() != null) {
                    throw new BadRequestException(
                            "Only internship details may be provided for INTERNSHIP opportunities.");
                }
            }
            case STUDENT_PROJECT -> {
                requireStudentProjectDetails(request);
                if (request.volunteering() != null || request.competition() != null || request.internship() != null) {
                    throw new BadRequestException(
                            "Only student project details may be provided for STUDENT_PROJECT opportunities.");
                }
            }
        }
    }

    private VolunteeringDetailsRequest requireVolunteeringDetails(CreateOpportunityRequest request) {
        if (request.volunteering() == null) {
            throw new BadRequestException("Volunteering opportunities require volunteering details.");
        }
        return request.volunteering();
    }

    private CompetitionDetailsRequest requireCompetitionDetails(CreateOpportunityRequest request) {
        if (request.competition() == null) {
            throw new BadRequestException("Competition opportunities require competition details.");
        }
        return request.competition();
    }

    private InternshipDetailsRequest requireInternshipDetails(CreateOpportunityRequest request) {
        if (request.internship() == null) {
            throw new BadRequestException("Internship opportunities require internship details.");
        }
        return request.internship();
    }

    private StudentProjectDetailsRequest requireStudentProjectDetails(CreateOpportunityRequest request) {
        if (request.studentProject() == null) {
            throw new BadRequestException("Student project opportunities require student project details.");
        }
        return request.studentProject();
    }

    private record PostingProfileSelection(
            OrganizationProfile organizationProfile,
            IndividualProfile individualProfile
    ) {}
}
