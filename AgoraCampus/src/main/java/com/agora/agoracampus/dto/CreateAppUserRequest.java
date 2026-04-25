package com.agora.agoracampus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAppUserRequest(
        @NotBlank @Size(max = 100) String keycloakId,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String username
) {
}
