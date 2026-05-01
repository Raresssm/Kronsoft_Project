package com.agora.agoracampus.messaging.dto.response;

import java.time.Instant;

public record MessageResponse(
        Long messageId,
        Long senderUserId,
        Long receiverUserId,
        String content,
        Instant sentAt,
        boolean acknowledged
) {}
