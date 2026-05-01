package com.agora.agoracampus.profile.organization.model;

import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.profile.core.model.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
