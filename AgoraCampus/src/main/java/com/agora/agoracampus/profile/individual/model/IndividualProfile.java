package com.agora.agoracampus.profile.individual.model;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.profile.background.model.Background;
import com.agora.agoracampus.profile.core.model.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "individual_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @OneToMany(mappedBy = "individualProfile", fetch = FetchType.LAZY)
    private List<Opportunity> opportunities = new ArrayList<>();

    @OneToMany(mappedBy = "individualProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Background> backgrounds = new ArrayList<>();
}
