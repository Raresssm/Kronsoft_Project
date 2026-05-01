package com.agora.agoracampus.profile.individual.service;

import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileCreateRequest;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileUpdateRequest;
import com.agora.agoracampus.profile.individual.mapper.IndividualProfileMapper;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndividualProfileService {

    private final IndividualProfileRepository individualProfileRepository;
    private final ProfileRepository profileRepository;
    private final IndividualProfileMapper mapper;

        public IndividualProfileResponse getByProfileId(Long profileId) {
         IndividualProfile profile=individualProfileRepository.findById(profileId).orElseThrow(() -> new RuntimeException("Individual profile not found"));
            return mapper.toResponse(profile);
        }

    public IndividualProfileResponse create(IndividualProfileCreateRequest dto) {
        Profile profile = profileRepository.findById(dto.profileId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        IndividualProfile individualProfile = mapper.toEntity(dto, profile);

        return mapper.toResponse(individualProfileRepository.save(individualProfile));
    }

        public IndividualProfileResponse update( Long id, IndividualProfileUpdateRequest individualProfile) {
            IndividualProfile existing =  individualProfileRepository.findById(id).orElseThrow(() -> new RuntimeException("Individual profile not found"));

            mapper.updateEntity(existing,individualProfile);
            return mapper.toResponse(individualProfileRepository.save(existing));
        }

        public void  delete(Long profileId) {
            IndividualProfile existing = individualProfileRepository.findById(profileId).orElseThrow(() -> new RuntimeException("Individual profile not found"));
            individualProfileRepository.delete(existing);

        }

}
