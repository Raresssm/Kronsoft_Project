package com.agora.agoracampus.profile.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCreateRequest {

        @NotNull
        private Long appUserId;

        @NotBlank(message = "Headline is required")
        @Size(max = 100, message = "Headline must be less than 100 characters")
        private String headline;

        @Size(max = 500, message = "Description must be less than 500 characters")
        private String description;

        @Size(max = 255, message = "Location must be less than 100 characters")
        private String location;

        @URL(message = "Website is not valid")
        @Size(max = 512, message = "Website must be less than 200 characters")
        private String website;

        @Size(max = 512)
        private String profilePicture;
        @Size(max = 512)
        private String coverImage;

    }





