package com.agora.agoracampus.profile.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateOrganizationProfileRequest(
        @NotNull(message = "App user id is required")
        Long appUserId,

        @NotBlank(message = "Headline is required")
        @Size(max = 255, message = "Headline must be less than 255 characters")
        String headline, // era "header" - gresit

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description,

        @Size(max = 255, message = "Location must be less than 255 characters")
        String location,

        @URL(message = "Website is not valid")
        @Size(max = 512, message = "Website must be less than 512 characters")
        String website,

        @Size(max = 512)
        String profilePicture,

        @Size(max = 512)
        String coverImage,

        @NotBlank(message = "Organization name is required")
        @Size(max = 255, message = "Organization name must be less than 255 characters")
        String organizationName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number is not valid")
        String phone,

        @NotBlank(message = "Industry is required")
        @Size(max = 255, message = "Industry must be less than 255 characters")
        String industry,

        @Size(max = 255, message = "Specialties must be less than 255 characters")
        String specialties
) {

}
