package com.agora.agoracampus.dto.response;

import com.agora.agoracampus.models.BackgroundType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

public record BackgroundResponse(

        Long backgroundId,
        Long individualProfileId,
       BackgroundType type,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Boolean currentlyOngoing

) {}
