package com.agora.agoracampus.profile.background.mapper;

import com.agora.agoracampus.profile.background.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.profile.background.dto.response.BackgroundResponse;
import com.agora.agoracampus.profile.background.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.profile.background.model.Background;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackgroundMapper {
    public BackgroundResponse toResponse(Background background) {

        IndividualProfile individual = background.getIndividualProfile();
        Profile profile = individual.getProfile();

        return new BackgroundResponse(
                individual.getId(),
                background.getId(),
                individual.getFirstName(),
                individual.getLastName(),
                profile.getHeadline(),
                profile.getProfilePicture(),
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
        background.setEndDate(Boolean.TRUE.equals(dto.currentlyOngoing()) ? null : dto.endDate());
        background.setCurrentlyOngoing(dto.currentlyOngoing());
        return background;
    }

    public void updateEntity(Background background, BackgroundUpdateRequest dto) {
        if (dto.type() != null) background.setType(dto.type());
        if (dto.title() != null) background.setTitle(dto.title());
        if (dto.description() != null) background.setDescription(dto.description());
        if (dto.startDate() != null) background.setStartDate(dto.startDate());
        if (dto.currentlyOngoing() != null) background.setCurrentlyOngoing(dto.currentlyOngoing());
        background.setEndDate(Boolean.TRUE.equals(dto.currentlyOngoing()) ? null : dto.endDate());
    }
}
