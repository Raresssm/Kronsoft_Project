package com.agora.agoracampus.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipResponse {

    private Integer internshipId;
    private Integer opportunityId;
    private String duration;
    private String compensation;
    private String requirements;
}
