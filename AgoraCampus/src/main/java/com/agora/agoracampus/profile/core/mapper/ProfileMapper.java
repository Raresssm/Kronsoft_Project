package com.agora.agoracampus.profile.core.mapper;

import com.agora.agoracampus.profile.core.dto.response.ProfileResponse;
import com.agora.agoracampus.profile.core.dto.request.ProfileUpdateRequest;
import com.agora.agoracampus.profile.core.model.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileMapper {


        public ProfileResponse toResponse(Profile profile) {
            return new ProfileResponse(
                    profile.getId(),
                    profile.getId().longValue(),
                    profile.getHeadline(),
                    profile.getDescription(),
                    profile.getLocation(),
                    profile.getWebsite(),
                    profile.getProfilePicture(),
                    profile.getCoverImage(),
                    profile.getUpdatedAt()
            );
        }

        public void updateEntity(Profile profile, ProfileUpdateRequest dto) {
            if (dto.headline() != null) profile.setHeadline(dto.headline());
            if (dto.description()!= null) profile.setDescription(dto.description());
            if (dto.location() != null) profile.setLocation(dto.location());
            if (dto.website() != null) profile.setWebsite(dto.website());
            if (dto.profilePicture() != null) profile.setProfilePicture(dto.profilePicture());
            if (dto.coverImage() != null) profile.setCoverImage(dto.coverImage());
        }
    }





