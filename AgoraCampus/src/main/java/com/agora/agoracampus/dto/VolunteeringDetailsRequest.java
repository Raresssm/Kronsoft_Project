package com.agora.agoracampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VolunteeringDetailsRequest(
        @NotBlank @Size(max = 255) String cause,
        @Size(max = 255) String schedule,
        @Size(max = 255) String benefits
) {
}
