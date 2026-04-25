package com.agora.agoracampus.models;

import com.agora.agoracampus.models.IndividualProfile;
import com.agora.agoracampus.models.OrganizationProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

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
  void updateTimestamp() {
    updatedAt = Instant.now();
  }
}

