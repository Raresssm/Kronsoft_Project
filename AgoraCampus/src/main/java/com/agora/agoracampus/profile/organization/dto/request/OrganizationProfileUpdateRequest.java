package com.agora.agoracampus.profile.organization.dto.request;


import jakarta.validation.constraints.*;

public record OrganizationProfileUpdateRequest(

        @NotNull(message = "Id  is required")
        Long id,
        @Size(max = 255, message = "Headline must be less than 255 characters")
        String headline,

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description,

        @Size(max = 255, message = "Location must be less than 255 characters")
        String location,

        @Size(max = 512, message = "Website must be less than 512 characters")
        String website,

        @Size(max = 512)
        String profilePicture,

        @Size(max = 512)
        String coverImage,

        @Size(max = 255, message = "Organization name must be less than 255 characters")
        String organizationName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number is not valid")
        String phone,

        @Size(max = 255, message = "Industry must be less than 255 characters")
        String industry,

        @Size(max = 255, message = "Specialties must be less than 255 characters")
        String specialties
) {}
