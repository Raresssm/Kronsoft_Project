package com.agora.agoracampus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "individual_profiles")
@Getter
@Setter
@NoArgsConstructor
public class IndividualProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "individual_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 50)
    private String phone;

    @Column(name = "cv_document", length = 512)
    private String cvDocument;

    @Column(length = 255)
    private String education;

    @Column(name = "education_period", length = 100)
    private String educationPeriod;

    @Column(name = "work_experience", length = 255)
    private String workExperience;

    @Column(name = "other_projects", length = 255)
    private String otherProjects;

    @OneToMany(mappedBy = "individualProfile", fetch = FetchType.LAZY)
    private List<Opportunity> opportunities = new ArrayList<>();
}
