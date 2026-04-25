package com.agora.agoracampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record IndividualProfileUpdateRequest(@NotNull(message = "Profile id is required")
                                        Long  profileId,

                                             @NotBlank(message = "First name is required")
                                        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
                                        String firstName,

                                             @NotBlank(message = "Last name is required")
                                        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
                                        String lastName,

                                             @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number is not valid")
                                        String phone,

                                             @Size(max = 512)
                                        String cvDocument) {

}

