package com.agora.agoracampus.opportunity.internship.model;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "internship_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false, unique = true)
    private Opportunity opportunity;

    @Column(length = 100)
    private String duration;

    @Column(length = 200)
    private String compensation;

    @Column(length = 500)
    private String requirements;
}
