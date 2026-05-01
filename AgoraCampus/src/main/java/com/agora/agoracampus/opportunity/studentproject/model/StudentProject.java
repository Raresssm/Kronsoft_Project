package com.agora.agoracampus.opportunity.studentproject.model;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false, unique = true)
    private Opportunity opportunity;

    @Column(name = "project_domain", nullable = false, length = 255)
    private String projectDomain;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "team_size")
    private Integer teamSize;
}
