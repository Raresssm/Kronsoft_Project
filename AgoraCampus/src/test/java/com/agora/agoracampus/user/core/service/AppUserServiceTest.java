package com.agora.agoracampus.user.core.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.security.SecurityIdentityService;
import com.agora.agoracampus.user.core.dto.request.CreateAppUserRequest;
import com.agora.agoracampus.user.core.dto.response.AppUserResponse;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private SecurityIdentityService securityIdentityService;

    private AppUserService service;

    @BeforeEach
    void setUp() {
        service = new AppUserService(appUserRepository, securityIdentityService);
    }

    @Test
    void createUserRejectsDuplicateKeycloakId() {
        when(securityIdentityService.requireKeycloakSubject()).thenReturn("kc-1");
        when(appUserRepository.existsByKeycloakId("kc-1")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(request()));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(securityIdentityService.requireKeycloakSubject()).thenReturn("kc-1");
        when(appUserRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(request()));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        when(securityIdentityService.requireKeycloakSubject()).thenReturn("kc-1");
        when(appUserRepository.existsByUsername("user")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(request()));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUserRejectsMismatchedEmailClaim() {
        when(securityIdentityService.requireKeycloakSubject()).thenReturn("kc-1");
        when(securityIdentityService.findEmailClaim()).thenReturn(Optional.of("other@example.com"));

        assertThrows(BadRequestException.class, () -> service.createUser(request()));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUserPersistsNewUser() {
        when(securityIdentityService.requireKeycloakSubject()).thenReturn("kc-1");
        AppUser saved = user(1L);

        when(appUserRepository.save(any(AppUser.class))).thenReturn(saved);

        AppUserResponse response = service.createUser(request());

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertEquals("kc-1", userCaptor.getValue().getKeycloakId());
        assertEquals("user@example.com", userCaptor.getValue().getEmail());
        assertEquals("user", response.username());
    }

    @Test
    void getRequiredEntityThrowsWhenMissing() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getRequiredEntity(1L));
    }

    private CreateAppUserRequest request() {
        return new CreateAppUserRequest("user@example.com", "user");
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setKeycloakId("kc-1");
        user.setEmail("user@example.com");
        user.setUsername("user");
        return user;
    }
}
