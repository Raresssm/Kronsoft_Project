package com.agora.agoracampus.opportunity.internship.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipResponse {

    private Long internshipId;
    private Long opportunityId;
    private String duration;
    private String compensation;
    private String requirements;
}
