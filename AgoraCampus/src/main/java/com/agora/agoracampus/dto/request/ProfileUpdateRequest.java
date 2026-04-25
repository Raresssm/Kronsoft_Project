package com.agora.agoracampus.dto.request;

import com.agora.agoracampus.models.IndividualProfile;
import com.agora.agoracampus.models.OrganizationProfile;
import com.agora.agoracampus.models.ProfileType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
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







