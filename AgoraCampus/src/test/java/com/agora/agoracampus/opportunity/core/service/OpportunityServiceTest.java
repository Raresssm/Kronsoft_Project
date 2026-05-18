package com.agora.agoracampus.opportunity.core.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.opportunity.application.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.opportunity.application.mapper.OpportunityApplicationMapper;
import com.agora.agoracampus.opportunity.application.model.ApplicationStatus;
import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import com.agora.agoracampus.opportunity.application.repository.OpportunityApplicationRepository;
import com.agora.agoracampus.opportunity.core.dto.request.CreateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.dto.request.UpdateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.mapper.OpportunityMapper;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.core.model.OpportunityType;
import com.agora.agoracampus.opportunity.core.repository.OpportunityRepository;
import com.agora.agoracampus.opportunity.competition.mapper.CompetitionMapper;
import com.agora.agoracampus.opportunity.internship.dto.request.InternshipDetailsRequest;
import com.agora.agoracampus.opportunity.internship.mapper.InternshipMapper;
import com.agora.agoracampus.opportunity.studentproject.mapper.StudentProjectMapper;
import com.agora.agoracampus.opportunity.volunteering.dto.request.VolunteeringDetailsRequest;
import com.agora.agoracampus.opportunity.volunteering.mapper.VolunteeringMapper;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.profile.organization.repository.OrganizationProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import com.agora.agoracampus.user.core.service.AppUserService;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private OrganizationProfileRepository organizationProfileRepository;

    @Mock
    private IndividualProfileRepository individualProfileRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private OpportunityApplicationRepository opportunityApplicationRepository;

    private final VolunteeringMapper volunteeringMapper = new VolunteeringMapper();
    private final CompetitionMapper competitionMapper = new CompetitionMapper();
    private final InternshipMapper internshipMapper = new InternshipMapper();
    private final StudentProjectMapper studentProjectMapper = new StudentProjectMapper();
    private final OpportunityMapper opportunityMapper = new OpportunityMapper(
            volunteeringMapper,
            competitionMapper,
            internshipMapper,
            studentProjectMapper
    );
    private final OpportunityApplicationMapper opportunityApplicationMapper = new OpportunityApplicationMapper();

    @Test
    void createOpportunityRequiresExactlyOnePostingProfile() {
        OpportunityService service = service();
        AppUser poster = user(1L);

        allowOrganizationActor(poster);
        when(appUserService.getRequiredEntity(1L)).thenReturn(poster);

        assertThrows(
                BadRequestException.class,
                () -> service.createOpportunity(opportunityRequest(null, null, OpportunityType.INTERNSHIP, null), 1L)
        );
        verify(opportunityRepository, never()).save(any(Opportunity.class));
    }

    @Test
    void createOpportunityRejectsPostingProfileOwnedByAnotherUser() {
        OpportunityService service = service();
        AppUser poster = user(1L);
        OrganizationProfile organizationProfile = organizationProfile(10L, user(2L));

        allowOrganizationActor(poster);
        when(appUserService.getRequiredEntity(1L)).thenReturn(poster);
        when(organizationProfileRepository.findById(10L)).thenReturn(Optional.of(organizationProfile));

        assertThrows(
                BadRequestException.class,
                () -> service.createOpportunity(opportunityRequest(10L, null, OpportunityType.INTERNSHIP, null), 1L)
        );
        verify(opportunityRepository, never()).save(any(Opportunity.class));
    }

    @Test
    void volunteeringOpportunityRequiresVolunteeringDetails() {
        OpportunityService service = service();
        AppUser poster = user(1L);

        allowOrganizationActor(poster);
        when(appUserService.getRequiredEntity(1L)).thenReturn(poster);
        when(organizationProfileRepository.findById(10L)).thenReturn(Optional.of(organizationProfile(10L, poster)));

        assertThrows(
                BadRequestException.class,
                () -> service.createOpportunity(opportunityRequest(10L, null, OpportunityType.VOLUNTEERING, null), 1L)
        );
        verify(opportunityRepository, never()).save(any(Opportunity.class));
    }

    @Test
    void nonVolunteeringOpportunityRejectsVolunteeringDetails() {
        OpportunityService service = service();
        AppUser poster = user(1L);
        VolunteeringDetailsRequest volunteering = new VolunteeringDetailsRequest("education", "weekends", "certificate");

        allowOrganizationActor(poster);
        when(appUserService.getRequiredEntity(1L)).thenReturn(poster);
        when(organizationProfileRepository.findById(10L)).thenReturn(Optional.of(organizationProfile(10L, poster)));

        assertThrows(
                BadRequestException.class,
                () -> service.createOpportunity(opportunityRequest(10L, null, OpportunityType.INTERNSHIP, volunteering), 1L)
        );
        verify(opportunityRepository, never()).save(any(Opportunity.class));
    }

    @Test
    void userCannotApplyToOwnOpportunity() {
        OpportunityService service = service();
        AppUser poster = user(1L);
        Opportunity opportunity = opportunity(50L, poster);

        allowOrganizationActor(poster);
        when(opportunityRepository.findById(50L)).thenReturn(Optional.of(opportunity));
        when(appUserService.getRequiredEntity(1L)).thenReturn(poster);

        assertThrows(
                BadRequestException.class,
                () -> service.applyToOpportunity(50L, new CreateOpportunityApplicationRequest(1L), 1L)
        );
        verify(opportunityApplicationRepository, never()).save(any(OpportunityApplication.class));
    }

    @Test
    void duplicateApplicationIsRejected() {
        OpportunityService service = service();
        AppUser applicant = user(2L);

        allowOrganizationActor(applicant);
        when(opportunityRepository.findById(50L)).thenReturn(Optional.of(opportunity(50L, user(1L))));
        when(appUserService.getRequiredEntity(2L)).thenReturn(applicant);
        when(opportunityApplicationRepository.existsByOpportunityIdAndApplicantUserId(50L, 2L)).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.applyToOpportunity(50L, new CreateOpportunityApplicationRequest(2L), 2L)
        );
        verify(opportunityApplicationRepository, never()).save(any(OpportunityApplication.class));
    }

    @Test
    void successfulApplicationStartsPending() {
        OpportunityService service = service();
        AppUser applicant = user(2L);
        Opportunity opportunity = opportunity(50L, user(1L));

        allowOrganizationActor(applicant);
        when(opportunityRepository.findById(50L)).thenReturn(Optional.of(opportunity));
        when(appUserService.getRequiredEntity(2L)).thenReturn(applicant);
        when(opportunityApplicationRepository.existsByOpportunityIdAndApplicantUserId(50L, 2L)).thenReturn(false);
        when(opportunityApplicationRepository.save(any(OpportunityApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.applyToOpportunity(50L, new CreateOpportunityApplicationRequest(2L), 2L);

        ArgumentCaptor<OpportunityApplication> applicationCaptor = ArgumentCaptor.forClass(OpportunityApplication.class);
        verify(opportunityApplicationRepository).save(applicationCaptor.capture());
        assertEquals(ApplicationStatus.PENDING, applicationCaptor.getValue().getStatus());
        assertEquals(2L, applicationCaptor.getValue().getApplicantUser().getId());
    }

    @Test
    void ownerCanAcceptApplication() {
        OpportunityService service = service();
        AppUser owner = user(1L);
        Opportunity opportunity = opportunity(50L, owner);
        OpportunityApplication application = application(99L, opportunity, user(2L), ApplicationStatus.PENDING);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(organizationProfile(10L, owner).getProfile()));
        when(opportunityApplicationRepository.findById(99L)).thenReturn(Optional.of(application));
        when(opportunityApplicationRepository.save(any(OpportunityApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateApplicationStatus(99L, 1L, new com.agora.agoracampus.opportunity.application.dto.request.UpdateOpportunityApplicationStatusRequest(ApplicationStatus.ACCEPTED));

        ArgumentCaptor<OpportunityApplication> applicationCaptor = ArgumentCaptor.forClass(OpportunityApplication.class);
        verify(opportunityApplicationRepository).save(applicationCaptor.capture());
        assertEquals(ApplicationStatus.ACCEPTED, applicationCaptor.getValue().getStatus());
    }

    @Test
    void applicationStatusCannotBeRevertedToPending() {
        OpportunityService service = service();
        AppUser owner = user(1L);
        Opportunity opportunity = opportunity(50L, owner);
        OpportunityApplication application = application(99L, opportunity, user(2L), ApplicationStatus.ACCEPTED);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(organizationProfile(10L, owner).getProfile()));
        when(opportunityApplicationRepository.findById(99L)).thenReturn(Optional.of(application));

        assertThrows(
                BadRequestException.class,
                () -> service.updateApplicationStatus(
                        99L,
                        1L,
                        new com.agora.agoracampus.opportunity.application.dto.request.UpdateOpportunityApplicationStatusRequest(ApplicationStatus.PENDING)
                )
        );
        verify(opportunityApplicationRepository, never()).save(any(OpportunityApplication.class));
    }

    @Test
    void ownerCanUpdateOpportunity() {
        OpportunityService service = service();
        AppUser owner = user(1L);
        Opportunity opportunity = opportunity(50L, owner);

        allowOrganizationActor(owner);
        when(opportunityRepository.findById(50L)).thenReturn(Optional.of(opportunity));
        when(opportunityRepository.save(any(Opportunity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateOpportunity(
                50L,
                1L,
                new UpdateOpportunityRequest("Updated", "Bucharest", "Autumn", "New description", "React")
        );

        ArgumentCaptor<Opportunity> opportunityCaptor = ArgumentCaptor.forClass(Opportunity.class);
        verify(opportunityRepository).save(opportunityCaptor.capture());
        assertEquals("Updated", opportunityCaptor.getValue().getTitle());
        assertEquals("Bucharest", opportunityCaptor.getValue().getLocation());
        assertEquals("Autumn", opportunityCaptor.getValue().getPeriod());
        assertEquals("New description", opportunityCaptor.getValue().getDescription());
        assertEquals("React", opportunityCaptor.getValue().getAdditionalInfo());
    }

    @Test
    void nonOwnerCannotUpdateOpportunity() {
        OpportunityService service = service();
        AppUser actor = user(2L);

        allowOrganizationActor(actor);
        when(opportunityRepository.findById(50L)).thenReturn(Optional.of(opportunity(50L, user(1L))));

        assertThrows(
                BadRequestException.class,
                () -> service.updateOpportunity(
                        50L,
                        2L,
                        new UpdateOpportunityRequest("Updated", "Bucharest", "Autumn", "New description", "React")
                )
        );
        verify(opportunityRepository, never()).save(any(Opportunity.class));
    }

    @Test
    void ownerCanDeleteOpportunityAndApplications() {
        OpportunityService service = service();
        AppUser owner = user(1L);
        Opportunity opportunity = opportunity(50L, owner);

        allowOrganizationActor(owner);
        when(opportunityRepository.findById(50L)).thenReturn(Optional.of(opportunity));

        service.deleteOpportunity(50L, 1L);

        verify(opportunityApplicationRepository).deleteByOpportunityId(50L);
        verify(opportunityRepository).delete(opportunity);
    }

    @Test
    void applicantCanDeleteOwnApplication() {
        OpportunityService service = service();
        AppUser applicant = user(2L);
        Opportunity opportunity = opportunity(50L, user(1L));
        OpportunityApplication application = application(99L, opportunity, applicant, ApplicationStatus.PENDING);

        allowOrganizationActor(applicant);
        when(opportunityApplicationRepository.findById(99L)).thenReturn(Optional.of(application));

        service.deleteApplication(99L, 2L);

        verify(opportunityApplicationRepository).delete(application);
    }

    @Test
    void nonApplicantCannotDeleteApplication() {
        OpportunityService service = service();
        AppUser actor = user(3L);
        Opportunity opportunity = opportunity(50L, user(1L));
        OpportunityApplication application = application(99L, opportunity, user(2L), ApplicationStatus.PENDING);

        allowOrganizationActor(actor);
        when(opportunityApplicationRepository.findById(99L)).thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class, () -> service.deleteApplication(99L, 3L));
        verify(opportunityApplicationRepository, never()).delete(any(OpportunityApplication.class));
    }

    private OpportunityService service() {
        return new OpportunityService(
                appUserService,
                appUserRepository,
                profileRepository,
                organizationProfileRepository,
                individualProfileRepository,
                opportunityRepository,
                opportunityApplicationRepository,
                opportunityMapper,
                opportunityApplicationMapper,
                volunteeringMapper,
                competitionMapper,
                internshipMapper,
                studentProjectMapper
        );
    }

    private CreateOpportunityRequest opportunityRequest(
            Long organizationProfileId,
            Long individualProfileId,
            OpportunityType type,
            VolunteeringDetailsRequest volunteering
    ) {
        InternshipDetailsRequest internship = type == OpportunityType.INTERNSHIP
                ? new InternshipDetailsRequest("3 months", "Paid", "Java")
                : null;
        return new CreateOpportunityRequest(
                1L,
                organizationProfileId,
                individualProfileId,
                type,
                "Title",
                "Cluj",
                "Summer",
                "Description",
                null,
                volunteering,
                null,
                internship,
                null
        );
    }

    private Opportunity opportunity(Long id, AppUser postedByUser) {
        Opportunity opportunity = new Opportunity();
        opportunity.setId(id);
        opportunity.setPostedByUser(postedByUser);
        opportunity.setType(OpportunityType.INTERNSHIP);
        opportunity.setTitle("Title");
        opportunity.setLocation("Cluj");
        opportunity.setPeriod("Summer");
        opportunity.setDescription("Description");
        opportunity.setOrganizationProfile(organizationProfile(10L, postedByUser));
        return opportunity;
    }

    private OpportunityApplication application(Long id, Opportunity opportunity, AppUser applicant, ApplicationStatus status) {
        OpportunityApplication application = new OpportunityApplication();
        application.setId(id);
        application.setOpportunity(opportunity);
        application.setApplicantUser(applicant);
        application.setStatus(status);
        application.setAppliedAt(Instant.parse("2026-05-17T00:00:00Z"));
        return application;
    }

    private OrganizationProfile organizationProfile(Long id, AppUser owner) {
        Profile profile = new Profile();
        profile.setId(100L + id);
        profile.setAppUser(owner);
        profile.setProfileType(ProfileType.ORGANIZATION);

        OrganizationProfile organizationProfile = new OrganizationProfile();
        organizationProfile.setId(id);
        organizationProfile.setProfile(profile);
        organizationProfile.setOrganizationName("Org " + id);
        profile.setOrganizationProfile(organizationProfile);
        return organizationProfile;
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setKeycloakId("keycloak-" + id);
        user.setEmail("user" + id + "@example.com");
        user.setUsername("user" + id);
        return user;
    }

    private void allowOrganizationActor(AppUser user) {
        OrganizationProfile organizationProfile = organizationProfile(10L, user);
        when(appUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(profileRepository.findByAppUser_Id(user.getId())).thenReturn(Optional.of(organizationProfile.getProfile()));
    }
}
