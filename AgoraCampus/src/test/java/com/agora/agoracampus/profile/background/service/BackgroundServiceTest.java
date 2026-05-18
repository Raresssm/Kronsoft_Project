package com.agora.agoracampus.profile.background.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.profile.background.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.profile.background.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.profile.background.mapper.BackgroundMapper;
import com.agora.agoracampus.profile.background.model.Background;
import com.agora.agoracampus.profile.background.model.BackgroundType;
import com.agora.agoracampus.profile.background.repository.BackgroundRepository;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackgroundServiceTest {

    @Mock
    private BackgroundRepository backgroundRepository;

    @Mock
    private IndividualProfileRepository individualProfileRepository;

    @Mock
    private AppUserRepository appUserRepository;

    private final BackgroundMapper backgroundMapper = new BackgroundMapper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanCreateBackground() {
        BackgroundService service = service();
        AppUser owner = user(1L);
        IndividualProfile individualProfile = individualProfile(10L, owner);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(individualProfileRepository.findById(10L)).thenReturn(Optional.of(individualProfile));
        when(backgroundRepository.save(any(Background.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createBackground(10L, 1L, createRequest());

        verify(backgroundRepository).save(any(Background.class));
    }

    @Test
    void nonOwnerCannotUpdateBackground() {
        BackgroundService service = service();
        Background background = background(20L, individualProfile(10L, user(1L)));

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(backgroundRepository.findById(20L)).thenReturn(Optional.of(background));

        assertThrows(BadRequestException.class, () -> service.updateBackground(10L, 20L, 2L, updateRequest("New title")));
        verify(backgroundRepository, never()).save(any(Background.class));
    }

    @Test
    void adminCanDeleteAnyBackground() {
        BackgroundService service = service();
        authenticateAsAdmin();
        Background background = background(20L, individualProfile(10L, user(1L)));

        when(appUserRepository.findById(99L)).thenReturn(Optional.of(user(99L)));
        when(backgroundRepository.findById(20L)).thenReturn(Optional.of(background));

        service.deleteBackground(10L, 20L, 99L);

        verify(backgroundRepository).delete(background);
    }

    @Test
    void backgroundMustBelongToRequestedProfile() {
        BackgroundService service = service();
        Background background = background(20L, individualProfile(10L, user(1L)));

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(backgroundRepository.findById(20L)).thenReturn(Optional.of(background));

        assertThrows(BadRequestException.class, () -> service.getBackgroundById(11L, 20L, 1L));
    }

    @Test
    void ownerCanUpdateBackground() {
        BackgroundService service = service();
        AppUser owner = user(1L);
        Background background = background(20L, individualProfile(10L, owner));

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(backgroundRepository.findById(20L)).thenReturn(Optional.of(background));
        when(backgroundRepository.save(background)).thenReturn(background);

        service.updateBackground(10L, 20L, 1L, updateRequest("New title"));

        assertEquals("New title", background.getTitle());
    }

    private BackgroundService service() {
        return new BackgroundService(backgroundRepository, individualProfileRepository, backgroundMapper, appUserRepository);
    }

    private BackgroundCreateRequest createRequest() {
        return new BackgroundCreateRequest(
                BackgroundType.EDUCATION,
                "School",
                "Description",
                LocalDate.of(2020, 1, 1),
                null,
                true
        );
    }

    private BackgroundUpdateRequest updateRequest(String title) {
        return new BackgroundUpdateRequest(
                BackgroundType.WORK_EXPERIENCE,
                title,
                "Description",
                LocalDate.of(2021, 1, 1),
                null,
                true
        );
    }

    private Background background(Long id, IndividualProfile individualProfile) {
        Background background = new Background();
        background.setId(id);
        background.setIndividualProfile(individualProfile);
        background.setType(BackgroundType.EDUCATION);
        background.setTitle("Old title");
        background.setDescription("Description");
        background.setStartDate(LocalDate.of(2020, 1, 1));
        background.setCurrentlyOngoing(true);
        return background;
    }

    private IndividualProfile individualProfile(Long id, AppUser owner) {
        Profile profile = new Profile();
        profile.setId(id + 100L);
        profile.setAppUser(owner);
        profile.setProfileType(ProfileType.INDIVIDUAL);
        profile.setHeadline("Headline");
        profile.setProfilePicture("profile.png");

        IndividualProfile individualProfile = new IndividualProfile();
        individualProfile.setId(id);
        individualProfile.setProfile(profile);
        individualProfile.setFirstName("First");
        individualProfile.setLastName("Last");
        profile.setIndividualProfile(individualProfile);
        return individualProfile;
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setKeycloakId("keycloak-" + id);
        user.setEmail("user" + id + "@example.com");
        user.setUsername("user" + id);
        return user;
    }

    private void authenticateAsAdmin() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
