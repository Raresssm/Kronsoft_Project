package com.agora.agoracampus.connection.dto.response;

import com.agora.agoracampus.connection.model.ConnectionStatus;

import java.time.Instant;

public record ConnectionResponse(
        Long connectionId,
        Long requesterUserId,
        Long receiverUserId,
        ConnectionStatus status,
        Instant createdAt
) {}
