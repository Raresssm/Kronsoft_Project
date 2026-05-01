package com.agora.agoracampus.opportunity.internship.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInternshipRequest {

    @NotNull(message = "opportunityId este obligatoriu.")
    private Long opportunityId;

    @Size(max = 100, message = "Durata nu poate depasi 100 de caractere.")
    private String duration;

    @Size(max = 200, message = "Compensatia nu poate depasi 200 de caractere.")
    private String compensation;

    @Size(max = 500, message = "Cerintele nu pot depasi 500 de caractere.")
    private String requirements;
}
