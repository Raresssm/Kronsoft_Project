package com.agora.agoracampus.messaging.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.messaging.dto.request.CreateMessageRequest;
import com.agora.agoracampus.messaging.dto.response.MessageResponse;
import com.agora.agoracampus.messaging.mapper.MessageMapper;
import com.agora.agoracampus.messaging.model.Message;
import com.agora.agoracampus.messaging.repository.MessageRepository;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final AppUserRepository appUserRepository;
    private final MessageMapper messageMapper;

    public List<MessageResponse> getConversation(Long userIdA, Long userIdB) {
        validateUser(userIdA);
        validateUser(userIdB);
        return messageRepository.findConversationBetween(userIdA, userIdB).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public List<MessageResponse> getUnreadIncoming(Long receiverUserId) {
        validateUser(receiverUserId);
        return messageRepository.findUnreadIncoming(receiverUserId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public MessageResponse getById(Long id) {
        return messageRepository.findById(id)
                .map(messageMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Message not found."));
    }

    @Transactional
    public MessageResponse send(CreateMessageRequest request) {
        if (request.senderUserId().equals(request.receiverUserId())) {
            throw new BadRequestException("Sender and receiver must be different users.");
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
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found."));
        if (!actingUserId.equals(message.getReceiver().getId())) {
            throw new BadRequestException("Only the receiver can mark a message as read.");
        }
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
}
