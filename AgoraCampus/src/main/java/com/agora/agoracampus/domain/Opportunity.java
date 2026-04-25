package com.agora.agoracampus.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opportunity_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    private AppUser postedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_profile_id")
    private OrganizationProfile organizationProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_profile_id")
    private IndividualProfile individualProfile;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OpportunityType type;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, length = 100)
    private String period;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "additional_info", length = 1000)
    private String additionalInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "opportunity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Volunteering volunteering;

    @OneToMany(mappedBy = "opportunity", fetch = FetchType.LAZY)
    private List<OpportunityApplication> applications = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if ((organizationProfile == null) == (individualProfile == null)) {
            throw new IllegalStateException("Opportunity must reference exactly one posting profile.");
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
