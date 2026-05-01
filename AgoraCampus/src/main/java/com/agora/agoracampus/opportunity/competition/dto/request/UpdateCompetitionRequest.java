package com.agora.agoracampus.opportunity.competition.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompetitionRequest {

    @NotBlank(message = "Tema este obligatorie.")
    @Size(max = 200, message = "Tema nu poate depasi 200 de caractere.")
    private String theme;

    @Size(max = 300, message = "Eligibilitatea nu poate depasi 300 de caractere.")
    private String eligibility;

    @Size(max = 200, message = "Premiul nu poate depasi 200 de caractere.")
    private String prize;

    @NotNull(message = "Deadline-ul este obligatoriu.")
    @Future(message = "Deadline-ul trebuie sa fie o data viitoare.")
    private LocalDate deadline;
}
