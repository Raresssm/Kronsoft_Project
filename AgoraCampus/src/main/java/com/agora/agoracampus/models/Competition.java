package com.agora.agoracampus.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entitatea Competition - corespunde tabelului COMPETITIONS din diagrama.
 *
 * Relatii din diagrama:
 *   - competition_id   PK
 *   - opportunity_id   FK -> OPPORTUNITIES
 *   - theme            String
 *   - eligibility      String
 *   - prize            String
 *   - deadline         datetime
 */
@Entity
@Table(name = "competitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer competitionId;

    // FK catre Opportunities
    @Column(nullable = false)
    private Integer opportunityId;

    private String theme;

    private String eligibility;

    private String prize;

    private LocalDate deadline;
}
