package com.agora.agoracampus.mappers;

import com.agora.agoracampus.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.dto.response.BackgroundResponse;
import com.agora.agoracampus.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.models.Background;
import com.agora.agoracampus.models.IndividualProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackgroundMapper {
    public BackgroundResponse toResponse(Background background) {
        return new BackgroundResponse(
                background.getBackgroundId(),
                background.getIndividualProfile().getId(),
                background.getType(),
                background.getTitle(),
                background.getDescription(),
                background.getStartDate(),
                background.getEndDate(),
                background.getCurrentlyOngoing()
        );
    }

    public Background toEntity(BackgroundCreateRequest dto, IndividualProfile individualProfile) {
        Background background = new Background();
        background.setIndividualProfile(individualProfile);
        background.setType(dto.type());
        background.setTitle(dto.title());
        background.setDescription(dto.description());
        background.setStartDate(dto.startDate());
        background.setEndDate(dto.endDate());
        background.setCurrentlyOngoing(dto.currentlyOngoing());
        return background;
    }

    public void updateEntity(Background background, BackgroundUpdateRequest dto) {
        if (dto.type() != null) background.setType(dto.type());
        if (dto.title() != null) background.setTitle(dto.title());
        if (dto.description() != null) background.setDescription(dto.description());
        if (dto.startDate() != null) background.setStartDate(dto.startDate());
        if (dto.endDate() != null) background.setEndDate(dto.endDate());
        if (dto.currentlyOngoing() != null) background.setCurrentlyOngoing(dto.currentlyOngoing());
    }
}

