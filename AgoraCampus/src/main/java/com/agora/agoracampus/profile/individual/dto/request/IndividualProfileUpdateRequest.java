package com.agora.agoracampus.profile.individual.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record IndividualProfileUpdateRequest(

        @Size(max = 255)
        String headline,

        @Size(max = 500)
        String description,

        @Size(max = 255)
        String location,

        @URL(message = "Website is not valid")
        String website,

        @Size(max = 512)
        String profilePicture,

        @Size(max = 512)
        String coverImage,

        @Size(min = 2, max = 50)
        String firstName,

        @Size(min = 2, max = 50)
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number is not valid")
        String phone,

        @Size(max = 512)
        String cvDocument)


{

}

