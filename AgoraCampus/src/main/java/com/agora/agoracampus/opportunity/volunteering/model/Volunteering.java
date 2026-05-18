package com.agora.agoracampus.opportunity.volunteering.model;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "volunteering")
@Getter
@Setter
@NoArgsConstructor
public class Volunteering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "volunteering_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false, unique = true)
    private Opportunity opportunity;

    @Column(nullable = false)
    private String cause;

    @Column(length = 255)
    private String schedule;

    @Column(length = 255)
    private String benefits;
}
