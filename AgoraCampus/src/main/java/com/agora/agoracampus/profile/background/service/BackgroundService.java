package com.agora.agoracampus.profile.background.service;

import com.agora.agoracampus.profile.background.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.profile.background.dto.response.BackgroundResponse;
import com.agora.agoracampus.profile.background.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.profile.background.mapper.BackgroundMapper;
import com.agora.agoracampus.profile.background.model.Background;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.background.repository.BackgroundRepository;
import com.agora.agoracampus.profile.individual.repository.IndividualProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackgroundService {


        private final BackgroundRepository backgroundRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final BackgroundMapper backgroundMapper;


    public List<BackgroundResponse> getBackgroundsByProfileId(Long individualProfileId) {

        individualProfileRepository.findById(individualProfileId)
                .orElseThrow(() -> new NotFoundException("Individual profile not found with id: " + individualProfileId));

        return backgroundRepository.findByIndividualProfile_Id(individualProfileId)
                .stream()
                .map(backgroundMapper::toResponse)
                .collect(Collectors.toList());
    }
        public BackgroundResponse getBackgroundById(Long id) {
            Background background =backgroundRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Background not found with id: " + id));

            return backgroundMapper.toResponse(background);
        }

    public BackgroundResponse createBackground(BackgroundCreateRequest dto) {
        IndividualProfile individualProfile = individualProfileRepository
                .findById(dto.individualProfileId())
                .orElseThrow(() -> new RuntimeException("Individual profile not found"));


        Background background = backgroundMapper.toEntity(dto, individualProfile);


        Background saved = backgroundRepository.save(background);


        return backgroundMapper.toResponse(saved);
    }


        public BackgroundResponse updateBackground(Long id, BackgroundUpdateRequest dto) {
            Background existing = backgroundRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Background not found with id: " + id));
            backgroundMapper.updateEntity(existing,dto);
            return backgroundMapper.toResponse( backgroundRepository.save(existing));
        }

        public void deleteBackground(Long id) {
            backgroundRepository.deleteById(id);
        }
    }

