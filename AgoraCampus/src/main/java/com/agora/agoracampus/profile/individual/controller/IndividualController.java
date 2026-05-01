package com.agora.agoracampus.profile.individual.controller;

import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileCreateRequest;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileUpdateRequest;
import com.agora.agoracampus.profile.individual.service.IndividualProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/")
@RequiredArgsConstructor
public class IndividualController {


    private IndividualProfileService profileService;


    @GetMapping("individual/{profileId}")
    public ResponseEntity<IndividualProfileResponse> getIndividualProfile(@PathVariable Long profileId) {

        return ResponseEntity.ok(profileService.getByProfileId(profileId));
    }

    @PostMapping("individual")
    public ResponseEntity<IndividualProfileResponse> createIndividualProfile(
            @RequestBody @Valid IndividualProfileCreateRequest dto) {
        return ResponseEntity.ok(profileService.create(dto));
    }



    @PutMapping("individual/{id}")
    public ResponseEntity<IndividualProfileResponse> updateIndividualProfile(@PathVariable Long  id, @RequestBody  @Valid IndividualProfileUpdateRequest dto) {

        return ResponseEntity.ok(profileService.update(id, dto));

    }



    @DeleteMapping("profile/individual/{id}")
    public ResponseEntity<IndividualProfileResponse> deleteIndividualProfile(@PathVariable Long  profileId){

        profileService.delete(profileId);

        return ResponseEntity.noContent().build();

    }


}
