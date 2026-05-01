package com.agora.agoracampus.profile.core.model;

import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.user.core.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_user_id", nullable = false, unique = true)
    private AppUser appUser;

    @Column(length = 255)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String location;

    @Column(length = 512)
    private String website;

    @Column(name = "profile_picture", length = 512)
    private String profilePicture;

    @Column(name = "cover_image", length = 512)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, length = 32)
    private ProfileType profileType;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private OrganizationProfile organizationProfile;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private IndividualProfile individualProfile;

    @PrePersist
    @PreUpdate
    void syncLifecycle() {
        updatedAt = Instant.now();
        validateProfileTypeConsistency();
    }

    private void validateProfileTypeConsistency() {
        boolean hasOrganization = organizationProfile != null;
        boolean hasIndividual = individualProfile != null;

        if (hasOrganization && hasIndividual) {
            throw new IllegalStateException(
                    "Profile cannot be both organization and individual."
            );
        }

        if (profileType == null) {
            if (hasOrganization) {
                profileType = ProfileType.ORGANIZATION;
            } else if (hasIndividual) {
                profileType = ProfileType.INDIVIDUAL;
            }
            return;
        }

        if (profileType == ProfileType.ORGANIZATION && hasIndividual) {
            throw new IllegalStateException(
                    "Profile type ORGANIZATION does not match attached individual subtype."
            );
        }
        if (profileType == ProfileType.INDIVIDUAL && hasOrganization) {
            throw new IllegalStateException(
                    "Profile type INDIVIDUAL does not match attached organization subtype."
            );
        }
    }
}
