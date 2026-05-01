package com.agora.agoracampus.opportunity.core.model;

import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import com.agora.agoracampus.opportunity.volunteering.model.Volunteering;
import com.agora.agoracampus.user.core.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
