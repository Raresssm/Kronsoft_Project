package com.agora.agoracampus.opportunity.competition.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionResponse {

    private Long competitionId;
    private Long opportunityId;
    private String theme;
    private String eligibility;
    private String prize;
    private LocalDate deadline;
}
