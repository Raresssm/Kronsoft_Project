package com.agora.agoracampus.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entitatea Internship - corespunde tabelului INTERNSHIPS din diagrama.
 *
 * Relatii din diagrama:
 *   - internship_id   PK
 *   - opportunity_id  FK -> OPPORTUNITIES
 *   - duration        String
 *   - compensation    String
 *   - requirements    String
 */
@Entity
@Table(name = "internships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer internshipId;

    @Column(nullable = false)
    private Integer opportunityId;

    private String duration;

    private String compensation;

    private String requirements;
}
