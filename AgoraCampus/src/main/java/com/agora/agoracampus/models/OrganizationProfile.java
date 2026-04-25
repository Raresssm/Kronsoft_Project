package com.agora.agoracampus.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "organization_profiles")
public class OrganizationProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;


    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String industry;

    @Column(length = 255)
    private String specialties;

    @OneToMany(mappedBy = "organizationProfile", fetch = FetchType.LAZY)
    private List<Opportunity> opportunities = new ArrayList<>();


}









