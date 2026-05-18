package com.agora.agoracampus.opportunity.competition.model;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    @Column(name = "competition_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false, unique = true)
    private Opportunity opportunity;

    @Column(length = 200)
    private String theme;

    @Column(length = 300)
    private String eligibility;

    @Column(length = 200)
    private String prize;

    private LocalDate deadline;
}
