package com.agora.agoracampus.connection.service;

import com.agora.agoracampus.connection.dto.request.CreateConnectionRequest;
import com.agora.agoracampus.connection.dto.request.UpdateConnectionStatusRequest;
import com.agora.agoracampus.connection.dto.response.ConnectionResponse;
import com.agora.agoracampus.connection.mapper.ConnectionMapper;
import com.agora.agoracampus.connection.model.ConnectionActorRole;
import com.agora.agoracampus.connection.model.Connection;
import com.agora.agoracampus.connection.model.ConnectionStatus;
import com.agora.agoracampus.connection.repository.ConnectionRepository;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;
    private final ConnectionMapper connectionMapper;

    public List<ConnectionResponse> listForUser(Long userId, Long actingUserId) {
        ConnectionActorRole actorRole = resolveActorRole(actingUserId);
        validateUserExists(userId);
        requireAdminOrSelf(
                actorRole,
                actingUserId,
                userId,
                "Only admins or the same user can view this user's connections."
        );
        return connectionRepository.findAllForUser(userId).stream().map(connectionMapper::toResponse).toList();
    }

    public List<ConnectionResponse> listPendingIncoming(Long receiverUserId, Long actingUserId) {
        ConnectionActorRole actorRole = resolveActorRole(actingUserId);
        validateUserExists(receiverUserId);
        requireAdminOrSelf(
                actorRole,
                actingUserId,
                receiverUserId,
                "Only admins or the same user can view pending incoming requests."
        );
        return connectionRepository.findByReceiver_IdAndStatus(receiverUserId, ConnectionStatus.PENDING)
                .stream()
                .map(connectionMapper::toResponse)
                .toList();
    }

    public ConnectionResponse getById(Long id, Long actingUserId) {
        ConnectionActorRole actorRole = resolveActorRole(actingUserId);
        Connection existing = connectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Connection not found."));
        requireAdminOrParticipant(
                existing,
                actorRole,
                actingUserId,
                "Only admins or participants can view this connection."
        );
        return connectionMapper.toResponse(existing);
    }

    @Transactional
    public ConnectionResponse create(Long actingUserId, CreateConnectionRequest request) {
        ConnectionActorRole actorRole = resolveActorRole(actingUserId);

        if (request.requesterUserId().equals(request.receiverUserId())) {
            throw new BadRequestException("Users cannot connect to themselves.");
        }
        if (!actingUserId.equals(request.requesterUserId())) {
            throw new BadRequestException("The acting user must be the requester.");
        }

        AppUser requester = validateUserExists(request.requesterUserId());
        AppUser receiver = validateUserExists(request.receiverUserId());
        requireConnectableRole(requester.getId());
        requireConnectableRole(receiver.getId());

        connectionRepository.findBetweenUsers(request.requesterUserId(), request.receiverUserId())
                .ifPresent(c -> {
                    throw new BadRequestException("A connection request already exists between these users.");
                });

        Connection connection = Connection.builder()
                .requester(requester)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .build();

        return connectionMapper.toResponse(connectionRepository.save(connection));
    }

    @Transactional
    public ConnectionResponse updateStatus(Long connectionId, Long actingUserId, UpdateConnectionStatusRequest request) {
        ConnectionActorRole actorRole = resolveActorRole(actingUserId);
        Connection existing = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new NotFoundException("Connection not found."));
        ConnectionStatus status = request.status();

        if (status == ConnectionStatus.PENDING) {
            throw new BadRequestException("Cannot revert to PENDING via this endpoint.");
        }
        if (actorRole != ConnectionActorRole.ADMIN && !actingUserId.equals(existing.getReceiver().getId())) {
            throw new BadRequestException("Only the receiver or an admin can accept or reject connection requests.");
        }

        existing.setStatus(status);

        return connectionMapper.toResponse(connectionRepository.save(existing));
    }

    @Transactional
    public void delete(Long connectionId, Long actingUserId) {
        ConnectionActorRole actorRole = resolveActorRole(actingUserId);
        Connection existing = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new NotFoundException("Connection not found."));
        requireAdminOrParticipant(existing, actorRole, actingUserId, "Only participants or an admin can delete this connection.");
        connectionRepository.delete(existing);
    }

    private AppUser validateUserExists(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " was not found."));
    }

    private ConnectionActorRole resolveActorRole(Long actingUserId) {
        validateUserExists(actingUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = isAuthenticated(authentication);
        boolean adminFromAuthentication = authenticated && hasAdminAuthority(authentication);

        if (authenticated) {
            validateAuthenticatedIdentity(actingUserId, adminFromAuthentication, authentication);
        }

        if (adminFromAuthentication) {
            return ConnectionActorRole.ADMIN;
        }
        return requireConnectableRole(actingUserId);
    }

    private ConnectionActorRole requireConnectableRole(Long userId) {
        Profile profile = profileRepository.findByAppUser_Id(userId)
                .orElseThrow(() -> new BadRequestException(
                        "User " + userId + " has no profile and cannot use the connection service."
                ));

        ProfileType profileType = profile.getProfileType();
        if (profileType == ProfileType.INDIVIDUAL) {
            return ConnectionActorRole.INDIVIDUAL;
        }
        if (profileType == ProfileType.ORGANIZATION) {
            return ConnectionActorRole.ORGANIZATION;
        }

        throw new BadRequestException(
                "User " + userId + " has an unsupported profile type for the connection service."
        );
    }

    private void requireAdminOrSelf(
            ConnectionActorRole actorRole,
            Long actingUserId,
            Long targetUserId,
            String message
    ) {
        if (actorRole != ConnectionActorRole.ADMIN && !actingUserId.equals(targetUserId)) {
            throw new BadRequestException(message);
        }
    }

    private void requireAdminOrParticipant(
            Connection connection,
            ConnectionActorRole actorRole,
            Long actingUserId,
            String message
    ) {
        if (actorRole == ConnectionActorRole.ADMIN) {
            return;
        }
        Long requesterId = connection.getRequester().getId();
        Long receiverId = connection.getReceiver().getId();
        if (!actingUserId.equals(requesterId) && !actingUserId.equals(receiverId)) {
            throw new BadRequestException(message);
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean hasAdminAuthority(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String normalizedAuthority = authority.getAuthority().toUpperCase(Locale.ROOT);
            if ("ROLE_ADMIN".equals(normalizedAuthority) || "ADMIN".equals(normalizedAuthority)) {
                return true;
            }
        }
        return false;
    }

    private void validateAuthenticatedIdentity(
            Long actingUserId,
            boolean adminFromAuthentication,
            Authentication authentication
    ) {
        if (adminFromAuthentication) {
            return;
        }
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
}
