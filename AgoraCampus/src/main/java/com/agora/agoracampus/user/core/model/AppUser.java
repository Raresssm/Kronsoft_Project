package com.agora.agoracampus.user.core.model;

import com.agora.agoracampus.opportunity.application.model.OpportunityApplication;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.profile.core.model.Profile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_user_id")
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 100)
    private String keycloakId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "appUser")
    private Profile profile;

    @OneToMany(mappedBy = "postedByUser", fetch = FetchType.LAZY)
    private List<Opportunity> postedOpportunities = new ArrayList<>();

    @OneToMany(mappedBy = "applicantUser", fetch = FetchType.LAZY)
    private List<OpportunityApplication> applications = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
