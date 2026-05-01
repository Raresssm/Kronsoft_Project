package com.agora.agoracampus.profile.core.dto.request;

import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public record ProfileUpdateRequest(

        @NotBlank
        @Size(max = 255)
        String headline,

        String description,

        @NotBlank
        @Size(max = 255)
        String location,

        @NotBlank
        @Size(max = 512)
        String website,

        @NotBlank
        @Size(max = 512)
        String profilePicture,

        @Size(max = 512)
        String coverImage,

        Instant updatedAt

) {}







