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
@Table(name = "organization_profiles")
@Getter
@Setter
@NoArgsConstructor
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
