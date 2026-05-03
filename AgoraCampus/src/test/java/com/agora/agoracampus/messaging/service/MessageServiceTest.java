package com.agora.agoracampus.messaging.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.messaging.dto.request.CreateMessageRequest;
import com.agora.agoracampus.messaging.mapper.MessageMapper;
import com.agora.agoracampus.messaging.model.Message;
import com.agora.agoracampus.messaging.repository.MessageRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ProfileRepository profileRepository;

    private final MessageMapper messageMapper = new MessageMapper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void individualUserCanSendAsSelf() {
        MessageService service = service();
        AppUser sender = user(1L);
        AppUser receiver = user(2L);

        when(appUserRepository.existsById(1L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(profile(sender, ProfileType.INDIVIDUAL)));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.send(1L, new CreateMessageRequest(1L, 2L, "hello"));

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertEquals(1L, messageCaptor.getValue().getSender().getId());
        assertEquals(2L, messageCaptor.getValue().getReceiver().getId());
    }

    @Test
    void organizationUserCanSendAsSelf() {
        MessageService service = service();
        AppUser sender = user(1L);
        AppUser receiver = user(2L);

        when(appUserRepository.existsById(1L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(profile(sender, ProfileType.ORGANIZATION)));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.send(1L, new CreateMessageRequest(1L, 2L, "hello")));
    }

    @Test
    void userCannotViewConversationTheyDoNotParticipateIn() {
        MessageService service = service();
        AppUser actingUser = user(3L);

        when(appUserRepository.existsById(3L)).thenReturn(true);
        when(appUserRepository.existsById(1L)).thenReturn(true);
        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(3L)).thenReturn(Optional.of(profile(actingUser, ProfileType.INDIVIDUAL)));

        assertThrows(BadRequestException.class, () -> service.getConversation(1L, 2L, 3L));
        verify(messageRepository, never()).findConversationBetween(1L, 2L);
    }

    @Test
    void adminCanViewAnyConversation() {
        MessageService service = service();
        authenticateAsAdmin();

        when(appUserRepository.existsById(99L)).thenReturn(true);
        when(appUserRepository.existsById(1L)).thenReturn(true);
        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(messageRepository.findConversationBetween(1L, 2L)).thenReturn(List.of());

        assertDoesNotThrow(() -> service.getConversation(1L, 2L, 99L));
        verify(messageRepository).findConversationBetween(1L, 2L);
    }

    @Test
    void adminCannotSendMessages() {
        MessageService service = service();
        authenticateAsAdmin();

        when(appUserRepository.existsById(99L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.send(99L, new CreateMessageRequest(99L, 2L, "hello")));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void userWithoutProfileCannotUseMessaging() {
        MessageService service = service();

        when(appUserRepository.existsById(1L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.getUnreadIncoming(1L, 1L));
    }

    @Test
    void adminCanMarkMessageRead() {
        MessageService service = service();
        authenticateAsAdmin();
        AppUser sender = user(1L);
        AppUser receiver = user(2L);
        Message message = Message.builder()
                .id(10L)
                .sender(sender)
                .receiver(receiver)
                .content("hello")
                .sentAt(Instant.now())
                .acknowledged(false)
                .build();

        when(appUserRepository.existsById(99L)).thenReturn(true);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        service.markRead(10L, 99L);

        assertTrue(message.isAcknowledged());
    }

    @Test
    void receiverCanMarkMessageRead() {
        MessageService service = service();
        AppUser sender = user(1L);
        AppUser receiver = user(2L);
        Message message = Message.builder()
                .id(10L)
                .sender(sender)
                .receiver(receiver)
                .content("hello")
                .sentAt(Instant.now())
                .acknowledged(false)
                .build();

        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(profile(receiver, ProfileType.INDIVIDUAL)));
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        service.markRead(10L, 2L);

        assertTrue(message.isAcknowledged());
    }

    @Test
    void outsiderCannotViewMessageById() {
        MessageService service = service();
        AppUser outsider = user(3L);
        Message message = Message.builder()
                .id(10L)
                .sender(user(1L))
                .receiver(user(2L))
                .content("hello")
                .sentAt(Instant.now())
                .acknowledged(false)
                .build();

        when(appUserRepository.existsById(3L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(3L)).thenReturn(Optional.of(profile(outsider, ProfileType.ORGANIZATION)));
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));

        assertThrows(BadRequestException.class, () -> service.getById(10L, 3L));
    }

    @Test
    void authenticatedPrincipalMustMatchActingUserForNonAdmins() {
        MessageService service = service();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("keycloak-1", null);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(appUserRepository.findByKeycloakId("keycloak-1")).thenReturn(Optional.of(user(1L)));

        assertThrows(BadRequestException.class, () -> service.getUnreadIncoming(2L, 2L));
    }

    private MessageService service() {
        return new MessageService(messageRepository, appUserRepository, profileRepository, messageMapper);
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

    private void authenticateAsAdmin() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
