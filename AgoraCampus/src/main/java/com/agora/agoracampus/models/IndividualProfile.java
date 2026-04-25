package com.agora.agoracampus.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name="individual_profiles")

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

    @Column
    @OneToMany(mappedBy = "individualProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Background> backgrounds;
}







