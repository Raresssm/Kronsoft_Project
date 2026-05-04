package com.agora.agoracampus.messaging.controller;

import com.agora.agoracampus.messaging.dto.request.CreateMessageRequest;
import com.agora.agoracampus.messaging.dto.response.MessageResponse;
import com.agora.agoracampus.messaging.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/between")
    public List<MessageResponse> conversation(
            @RequestParam Long userIdA,
            @RequestParam Long userIdB,
            @RequestParam Long actingUserId
    ) {
        return messageService.getConversation(userIdA, userIdB, actingUserId);
    }

    @GetMapping("/incoming/{receiverUserId}/unread")
    public List<MessageResponse> unread(@PathVariable Long receiverUserId, @RequestParam Long actingUserId) {
        return messageService.getUnreadIncoming(receiverUserId, actingUserId);
    }

    @GetMapping("/{id}")
    public MessageResponse getById(@PathVariable Long id, @RequestParam Long actingUserId) {
        return messageService.getById(id, actingUserId);
    }

    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @RequestParam Long actingUserId,
            @Valid @RequestBody CreateMessageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.send(actingUserId, request));
    }

    @PatchMapping("/{id}/read")
    public MessageResponse markRead(
            @PathVariable Long id,
            @RequestParam(required = false) Long actingUserId,
            @RequestParam(required = false) Long actingReceiverUserId
    ) {
        return messageService.markRead(id, actingUserId != null ? actingUserId : actingReceiverUserId);
    }
}
