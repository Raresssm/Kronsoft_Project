package com.agora.agoracampus.user.core.service;

import com.agora.agoracampus.exception.ConflictException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.user.core.dto.request.CreateAppUserRequest;
import com.agora.agoracampus.user.core.dto.response.AppUserResponse;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
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

    @Test
    void createUserRejectsDuplicateKeycloakId() {
        AppUserService service = new AppUserService(appUserRepository);

        when(appUserRepository.existsByKeycloakId("kc-1")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(request()));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        AppUserService service = new AppUserService(appUserRepository);

        when(appUserRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(request()));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        AppUserService service = new AppUserService(appUserRepository);

        when(appUserRepository.existsByUsername("user")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createUser(request()));
        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void createUserPersistsNewUser() {
        AppUserService service = new AppUserService(appUserRepository);
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
        AppUserService service = new AppUserService(appUserRepository);

        when(appUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getRequiredEntity(1L));
    }

    private CreateAppUserRequest request() {
        return new CreateAppUserRequest("kc-1", "user@example.com", "user");
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
