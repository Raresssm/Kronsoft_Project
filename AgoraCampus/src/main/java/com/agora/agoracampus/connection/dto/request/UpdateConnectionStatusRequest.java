package com.agora.agoracampus.connection.dto.request;

import com.agora.agoracampus.connection.model.ConnectionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateConnectionStatusRequest(
        @NotNull ConnectionStatus status
) {}
