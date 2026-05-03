package com.agora.agoracampus.profile.core.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.profile.core.dto.request.ProfileUpdateRequest;
import com.agora.agoracampus.profile.core.mapper.ProfileMapper;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private AppUserRepository appUserRepository;

    private final ProfileMapper profileMapper = new ProfileMapper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanUpdateProfile() {
        ProfileService service = service();
        Profile profile = individualProfile(10L, user(1L));

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(profile.getAppUser()));
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);

        service.updateProfile(10L, 1L, updateRequest("New headline"));

        assertEquals("New headline", profile.getHeadline());
    }

    @Test
    void nonOwnerCannotUpdateProfile() {
        ProfileService service = service();
        Profile actorProfile = individualProfile(20L, user(2L));
        Profile targetProfile = individualProfile(10L, user(1L));

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(actorProfile.getAppUser()));
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(actorProfile));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(targetProfile));

        assertThrows(BadRequestException.class, () -> service.updateProfile(10L, 2L, updateRequest("New headline")));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void adminCanDeleteAnyProfile() {
        ProfileService service = service();
        authenticateAsAdmin();
        Profile targetProfile = individualProfile(10L, user(1L));

        when(appUserRepository.findById(99L)).thenReturn(Optional.of(user(99L)));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(targetProfile));

        service.deleteProfile(10L, 99L);

        verify(profileRepository).delete(targetProfile);
    }

    @Test
    void userWithoutProfileCannotUpdateProfile() {
        ProfileService service = service();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.updateProfile(10L, 1L, updateRequest("New headline")));
    }

    @Test
    void authenticatedPrincipalMustMatchActingUserForNonAdmins() {
        ProfileService service = service();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("keycloak-1", null);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(appUserRepository.findByKeycloakId("keycloak-1")).thenReturn(Optional.of(user(1L)));

        assertThrows(BadRequestException.class, () -> service.updateProfile(10L, 2L, updateRequest("New headline")));
    }

    private ProfileService service() {
        return new ProfileService(profileRepository, appUserRepository, profileMapper);
    }

    private ProfileUpdateRequest updateRequest(String headline) {
        return new ProfileUpdateRequest(
                headline,
                "Description",
                "Cluj",
                "https://example.com",
                "https://example.com/profile.png",
                "https://example.com/cover.png",
                null
        );
    }

    private Profile individualProfile(Long id, AppUser owner) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setAppUser(owner);
        profile.setProfileType(ProfileType.INDIVIDUAL);
        profile.setHeadline("Old headline");

        IndividualProfile individualProfile = new IndividualProfile();
        individualProfile.setId(id + 100L);
        individualProfile.setProfile(profile);
        profile.setIndividualProfile(individualProfile);
        return profile;
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
