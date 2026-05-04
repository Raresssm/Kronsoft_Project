package com.agora.agoracampus.messaging.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.messaging.dto.request.CreateMessageRequest;
import com.agora.agoracampus.messaging.dto.response.MessageResponse;
import com.agora.agoracampus.messaging.mapper.MessageMapper;
import com.agora.agoracampus.messaging.model.MessageActorRole;
import com.agora.agoracampus.messaging.model.MessageUserSubRole;
import com.agora.agoracampus.messaging.model.Message;
import com.agora.agoracampus.messaging.repository.MessageRepository;
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
public class MessageService {

    private final MessageRepository messageRepository;
    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;
    private final MessageMapper messageMapper;

    public List<MessageResponse> getConversation(Long userIdA, Long userIdB, Long actingUserId) {
        MessageActor actor = resolveActor(actingUserId);
        validateUser(userIdA);
        validateUser(userIdB);
        requireAdminOrParticipant(
                actor,
                actingUserId,
                userIdA,
                userIdB,
                "Only admins or conversation participants can view this conversation."
        );
        return messageRepository.findConversationBetween(userIdA, userIdB).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public List<MessageResponse> getUnreadIncoming(Long receiverUserId, Long actingUserId) {
        MessageActor actor = resolveActor(actingUserId);
        validateUser(receiverUserId);
        requireAdminOrSelf(
                actor,
                actingUserId,
                receiverUserId,
                "Only admins or the receiver can view unread incoming messages."
        );
        return messageRepository.findUnreadIncoming(receiverUserId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public MessageResponse getById(Long id, Long actingUserId) {
        MessageActor actor = resolveActor(actingUserId);
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found."));
        requireAdminOrMessageParticipant(
                actor,
                actingUserId,
                message,
                "Only admins or message participants can view this message."
        );
        return messageMapper.toResponse(message);
    }

    @Transactional
    public MessageResponse send(Long actingUserId, CreateMessageRequest request) {
        MessageActor actor = resolveActor(actingUserId);
        if (request.senderUserId().equals(request.receiverUserId())) {
            throw new BadRequestException("Sender and receiver must be different users.");
        }
        if (actor.role() == MessageActorRole.ADMIN) {
            throw new BadRequestException("Admins cannot send messages.");
        }
        if (!actingUserId.equals(request.senderUserId())) {
            throw new BadRequestException("The acting user must be the sender.");
        }

        Message message = Message.builder()
                .sender(appUserRepository.findById(request.senderUserId()).orElseThrow(() -> userNotFound(request.senderUserId())))
                .receiver(appUserRepository.findById(request.receiverUserId()).orElseThrow(() -> userNotFound(request.receiverUserId())))
                .content(request.content())
                .acknowledged(false)
                .build();

        return messageMapper.toResponse(messageRepository.save(message));
    }

    @Transactional
    public MessageResponse markRead(Long messageId, Long actingUserId) {
        MessageActor actor = resolveActor(actingUserId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found."));
        requireAdminOrSelf(
                actor,
                actingUserId,
                message.getReceiver().getId(),
                "Only admins or the receiver can mark a message as read."
        );
        message.setAcknowledged(true);
        return messageMapper.toResponse(messageRepository.save(message));
    }

    private void validateUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw userNotFound(id);
        }
    }

    private NotFoundException userNotFound(Long id) {
        return new NotFoundException("User " + id + " was not found.");
    }

    private MessageActor resolveActor(Long actingUserId) {
        if (actingUserId == null) {
            throw new BadRequestException("actingUserId is required.");
        }
        validateUser(actingUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = isAuthenticated(authentication);
        boolean adminFromAuthentication = authenticated && hasAdminAuthority(authentication);

        if (authenticated) {
            validateAuthenticatedIdentity(actingUserId, adminFromAuthentication, authentication);
        }

        if (adminFromAuthentication) {
            return new MessageActor(MessageActorRole.ADMIN, null);
        }
        return new MessageActor(MessageActorRole.USER, requireMessagingUserSubRole(actingUserId));
    }

    private MessageUserSubRole requireMessagingUserSubRole(Long userId) {
        Profile profile = profileRepository.findByAppUser_Id(userId)
                .orElseThrow(() -> new BadRequestException(
                        "User " + userId + " has no profile and cannot use the messaging service."
                ));

        ProfileType profileType = profile.getProfileType();
        if (profileType == ProfileType.INDIVIDUAL) {
            return MessageUserSubRole.INDIVIDUAL;
        }
        if (profileType == ProfileType.ORGANIZATION) {
            return MessageUserSubRole.ORGANIZATION;
        }

        throw new BadRequestException(
                "User " + userId + " has an unsupported profile type for the messaging service."
        );
    }

    private void requireAdminOrParticipant(
            MessageActor actor,
            Long actingUserId,
            Long userIdA,
            Long userIdB,
            String message
    ) {
        if (actor.role() == MessageActorRole.ADMIN) {
            return;
        }
        if (!actingUserId.equals(userIdA) && !actingUserId.equals(userIdB)) {
            throw new BadRequestException(message);
        }
    }

    private void requireAdminOrMessageParticipant(
            MessageActor actor,
            Long actingUserId,
            Message message,
            String errorMessage
    ) {
        requireAdminOrParticipant(
                actor,
                actingUserId,
                message.getSender().getId(),
                message.getReceiver().getId(),
                errorMessage
        );
    }

    private void requireAdminOrSelf(
            MessageActor actor,
            Long actingUserId,
            Long targetUserId,
            String message
    ) {
        if (actor.role() != MessageActorRole.ADMIN && !actingUserId.equals(targetUserId)) {
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

    private record MessageActor(MessageActorRole role, MessageUserSubRole userSubRole) {
    }
}
