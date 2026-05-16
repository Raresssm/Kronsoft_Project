package com.agora.agoracampus.opportunity.core.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.opportunity.application.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.opportunity.application.mapper.OpportunityApplicationMapper;
import com.agora.agoracampus.opportunity.application.model.ApplicationStatus;
import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import com.agora.agoracampus.opportunity.application.repository.OpportunityApplicationRepository;
import com.agora.agoracampus.opportunity.core.dto.request.CreateOpportunityRequest;
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
