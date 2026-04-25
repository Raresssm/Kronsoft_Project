package com.agora.agoracampus.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "opportunity_applications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_opportunity_applicant",
                columnNames = {"opportunity_id", "applicant_user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class OpportunityApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private AppUser applicantUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private Instant appliedAt;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
        if (appliedAt == null) {
            appliedAt = Instant.now();
        }
    }
}
