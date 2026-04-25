package com.agora.agoracampus.service;

import com.agora.agoracampus.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.dto.response.BackgroundResponse;
import com.agora.agoracampus.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.mappers.BackgroundMapper;
import com.agora.agoracampus.models.Background;
import com.agora.agoracampus.models.IndividualProfile;
import com.agora.agoracampus.repository.BackgroundRepository;
import com.agora.agoracampus.repository.IndividualProfileRepository;
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

