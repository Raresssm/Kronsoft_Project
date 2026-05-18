package com.agora.agoracampus.profile.background.dto.request;


import com.agora.agoracampus.profile.background.model.BackgroundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

public record BackgroundUpdateRequest(


        @NotNull(message = "Type is required")
        BackgroundType type,

        @NotBlank(message = "Title is required")
        @Size(max = 30, message = "Title must be less than 30 characters")
        String title,

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        Boolean currentlyOngoing

) {}






