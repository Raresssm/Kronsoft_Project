package com.agora.agoracampus.profile.core.service;


import com.agora.agoracampus.profile.core.dto.request.ProfileUpdateRequest;
import com.agora.agoracampus.profile.core.dto.response.ProfileResponse;
import com.agora.agoracampus.profile.core.mapper.ProfileMapper;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
    @RequiredArgsConstructor
    public class ProfileService {

        private final ProfileRepository profileRepository;
        private final ProfileMapper mapper;




    /*

    public IndividualProfileResponse create(Integer appUserId, ProfileCreateRequest dto) {
        Profile profile = profileRepository.findById(dto.getProfileId(appUserId))
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        IndividualProfile individualProfile = mapper.toEntity(dto, profile);

        return mapper.toResponse(individualProfileRepository.save(individualProfile));
    }

*/


    public ProfileResponse getProfileById(Long id) {
            Profile profile=profileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
            return mapper.toResponse(profile);
        }

        public ProfileResponse updateProfile(Long id, ProfileUpdateRequest dtoRequest) {
            Profile existing = profileRepository.findById(id).orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

         mapper.updateEntity(existing,dtoRequest);
            return mapper.toResponse(profileRepository.save(existing));
        }

        public void deleteProfile(Long id) {
            profileRepository.deleteById(id);
        }





}
