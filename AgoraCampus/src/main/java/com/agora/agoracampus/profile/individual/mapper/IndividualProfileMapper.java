package com.agora.agoracampus.profile.individual.mapper;

import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileCreateRequest;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileUpdateRequest;
import com.agora.agoracampus.profile.background.mapper.BackgroundMapper;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.core.model.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
@Component

public class IndividualProfileMapper {

        private final BackgroundMapper backgroundMapper;


        public IndividualProfileMapper(BackgroundMapper backgroundMapper) {
            this.backgroundMapper = backgroundMapper;
        }

        public IndividualProfileResponse toResponse(IndividualProfile individualProfile) {
            return new IndividualProfileResponse(
                    individualProfile.getId(),
                    individualProfile.getFirstName(),
                    individualProfile.getLastName(),
                    individualProfile.getPhone(),
                    individualProfile.getCvDocument(),
                    individualProfile.getBackgrounds() == null
                            ? List.of()
                            : individualProfile.getBackgrounds()
                            .stream()
                            .map(backgroundMapper::toResponse)
                            .toList()
            );
        }

        public IndividualProfile toEntity(IndividualProfileCreateRequest dto, Profile profile) {
            IndividualProfile individualProfile = new IndividualProfile();
            individualProfile.setProfile(profile);
            individualProfile.setFirstName(dto.firstName());
            individualProfile.setLastName(dto.lastName());
            individualProfile.setPhone(dto.phone());
            individualProfile.setCvDocument(dto.cvDocument());
            return individualProfile;
        }

        public void updateEntity(IndividualProfile individualProfile, IndividualProfileUpdateRequest dto) {
            if (dto.firstName() != null) individualProfile.setFirstName(dto.firstName());
            if (dto.lastName() != null) individualProfile.setLastName(dto.lastName());
            if (dto.phone() != null) individualProfile.setPhone(dto.phone());
            if (dto.cvDocument() != null) individualProfile.setCvDocument(dto.cvDocument());
        }
    }



