package com.agora.agoracampus.connection.service;

import com.agora.agoracampus.connection.dto.request.CreateConnectionRequest;
import com.agora.agoracampus.connection.dto.request.UpdateConnectionStatusRequest;
import com.agora.agoracampus.connection.dto.response.ConnectionResponse;
import com.agora.agoracampus.connection.mapper.ConnectionMapper;
import com.agora.agoracampus.connection.model.Connection;
import com.agora.agoracampus.connection.model.ConnectionStatus;
import com.agora.agoracampus.connection.repository.ConnectionRepository;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ProfileRepository profileRepository;

    private final ConnectionMapper connectionMapper = new ConnectionMapper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void individualUserCanCreatePendingRequestAsRequester() {
        ConnectionService service = service();
        AppUser requester = user(1L);
        AppUser receiver = user(2L);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(profile(requester, ProfileType.INDIVIDUAL)));
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(profile(receiver, ProfileType.ORGANIZATION)));
        when(connectionRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(connectionRepository.save(any(Connection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConnectionResponse response = service.create(1L, new CreateConnectionRequest(1L, 2L));

        assertEquals(1L, response.requesterUserId());
        assertEquals(2L, response.receiverUserId());
        assertEquals(ConnectionStatus.PENDING, response.status());
    }

    @Test
    void adminCannotCreateConnectionRequest() {
        ConnectionService service = service();
        authenticateAsAdmin();

        when(appUserRepository.findById(99L)).thenReturn(Optional.of(user(99L)));

        assertThrows(BadRequestException.class, () -> service.create(99L, new CreateConnectionRequest(99L, 1L)));
        verify(connectionRepository, never()).save(any(Connection.class));
    }

    @Test
    void receiverCanAcceptPendingRequest() {
        ConnectionService service = service();
        AppUser requester = user(1L);
        AppUser receiver = user(2L);
        Connection connection = connection(10L, requester, receiver, ConnectionStatus.PENDING);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(profile(receiver, ProfileType.ORGANIZATION)));
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(connection));
        when(connectionRepository.save(connection)).thenReturn(connection);

        service.updateStatus(10L, 2L, new UpdateConnectionStatusRequest(ConnectionStatus.ACCEPTED));

        assertEquals(ConnectionStatus.ACCEPTED, connection.getStatus());
    }

    @Test
    void requesterCannotAcceptOwnRequest() {
        ConnectionService service = service();
        AppUser requester = user(1L);
        AppUser receiver = user(2L);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(profile(requester, ProfileType.INDIVIDUAL)));
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(connection(10L, requester, receiver, ConnectionStatus.PENDING)));

        assertThrows(
                BadRequestException.class,
                () -> service.updateStatus(10L, 1L, new UpdateConnectionStatusRequest(ConnectionStatus.ACCEPTED))
        );
        verify(connectionRepository, never()).save(any(Connection.class));
    }

    @Test
    void outsiderCannotViewConnection() {
        ConnectionService service = service();
        AppUser outsider = user(3L);

        when(appUserRepository.findById(3L)).thenReturn(Optional.of(outsider));
        when(profileRepository.findByAppUser_Id(3L)).thenReturn(Optional.of(profile(outsider, ProfileType.INDIVIDUAL)));
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(connection(10L, user(1L), user(2L), ConnectionStatus.ACCEPTED)));

        assertThrows(BadRequestException.class, () -> service.getById(10L, 3L));
    }

    @Test
    void adminCanViewAnyConnection() {
        ConnectionService service = service();
        authenticateAsAdmin();

        when(appUserRepository.findById(99L)).thenReturn(Optional.of(user(99L)));
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(connection(10L, user(1L), user(2L), ConnectionStatus.ACCEPTED)));

        assertDoesNotThrow(() -> service.getById(10L, 99L));
    }

    @Test
    void userWithoutProfileCannotUseConnections() {
        ConnectionService service = service();
        AppUser user = user(1L);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.listForUser(1L, 1L));
    }

    @Test
    void authenticatedPrincipalMustMatchActingUserForNonAdmins() {
        ConnectionService service = service();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("keycloak-1", null);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(appUserRepository.findByKeycloakId("keycloak-1")).thenReturn(Optional.of(user(1L)));

        assertThrows(BadRequestException.class, () -> service.listForUser(2L, 2L));
    }

    @Test
    void adminCanDeleteAnyConnection() {
        ConnectionService service = service();
        authenticateAsAdmin();
        Connection connection = connection(10L, user(1L), user(2L), ConnectionStatus.ACCEPTED);

        when(appUserRepository.findById(99L)).thenReturn(Optional.of(user(99L)));
        when(connectionRepository.findById(10L)).thenReturn(Optional.of(connection));

        service.delete(10L, 99L);

        ArgumentCaptor<Connection> connectionCaptor = ArgumentCaptor.forClass(Connection.class);
        verify(connectionRepository).delete(connectionCaptor.capture());
        assertEquals(10L, connectionCaptor.getValue().getId());
    }

    private ConnectionService service() {
        return new ConnectionService(connectionRepository, appUserRepository, profileRepository, connectionMapper);
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setKeycloakId("keycloak-" + id);
        user.setEmail("user" + id + "@example.com");
        user.setUsername("user" + id);
        return user;
    }

    private Profile profile(AppUser user, ProfileType profileType) {
        Profile profile = new Profile();
        profile.setAppUser(user);
        profile.setProfileType(profileType);
        return profile;
    }

    private Connection connection(Long id, AppUser requester, AppUser receiver, ConnectionStatus status) {
        return Connection.builder()
                .id(id)
                .requester(requester)
                .receiver(receiver)
                .status(status)
                .build();
    }

    private void authenticateAsAdmin() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
