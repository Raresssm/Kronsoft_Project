package com.agora.agoracampus.connection.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateConnectionRequest(
        @NotNull Long requesterUserId,
        @NotNull Long receiverUserId
) {}
