package com.agora.agoracampus.profile.background.dto.response;

import com.agora.agoracampus.profile.background.model.BackgroundType;
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
