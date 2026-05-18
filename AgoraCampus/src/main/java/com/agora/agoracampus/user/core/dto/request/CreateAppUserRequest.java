package com.agora.agoracampus.user.core.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAppUserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String username,
        @Pattern(regexp = "INDIVIDUAL|ORGANIZATION") String accountType
) {
}
