package com.agora.agoracampus.messaging.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMessageRequest(
        @NotNull Long senderUserId,
        @NotNull Long receiverUserId,
        @NotBlank String content
) {}
