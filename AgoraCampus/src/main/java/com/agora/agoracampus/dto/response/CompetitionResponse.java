package com.agora.agoracampus.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionResponse {

    private Integer competitionId;
    private Integer opportunityId;
    private String theme;
    private String eligibility;
    private String prize;
    private LocalDate deadline;
}
