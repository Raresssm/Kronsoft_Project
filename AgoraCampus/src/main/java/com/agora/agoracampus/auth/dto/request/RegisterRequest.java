package com.agora.agoracampus.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 1, max = 100) String password,
        @NotBlank @Pattern(regexp = "INDIVIDUAL|ORGANIZATION") String accountType
) {
}
