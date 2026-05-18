package com.agora.agoracampus.opportunity.internship.dto.request;

import jakarta.validation.constraints.Size;

public record InternshipDetailsRequest(
        @Size(max = 100) String duration,
        @Size(max = 200) String compensation,
        @Size(max = 500) String requirements
) {
}
