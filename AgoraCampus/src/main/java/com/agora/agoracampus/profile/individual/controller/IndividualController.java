package com.agora.agoracampus.profile.individual.controller;

import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileCreateRequest;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileUpdateRequest;
import com.agora.agoracampus.profile.individual.service.IndividualProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles/")
@RequiredArgsConstructor
public class IndividualController {


    private IndividualProfileService profileService;


    @GetMapping("individual/name/{name}")
    public ResponseEntity<List<IndividualProfileResponse>> getIndividualProfileByName(@PathVariable String name) {


        return ResponseEntity.ok(profileService.getProfileByName(name));
    }
/*
    @GetMapping("individual/title/{title}")
    public ResponseEntity<List<IndividualProfileResponse>> getIndividualProfileByTitle(@PathVariable String title) {


        return ResponseEntity.ok(profileService.getProfileByTitle(title));
    }

    /*


 */
    @GetMapping("individual/location/{location}")
    public ResponseEntity<List<IndividualProfileResponse>> getIndividualProfileByLocation(@PathVariable String location) {


        return ResponseEntity.ok(profileService.getProfileByLocation(location));
    }
/*
    @GetMapping("individual/opportunity/{opportunity}")
    public ResponseEntity<List<IndividualProfileResponse>> getIndividualProfileByOpportunity(@PathVariable String opportunity) {

        return ResponseEntity.ok(profileService.getProfileByOpportunityName(opportunity));
    }
/*

 */
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



    @DeleteMapping("individual/{id}")
    public ResponseEntity<IndividualProfileResponse> deleteIndividualProfile(@PathVariable Long  profileId){

        profileService.delete(profileId);

        return ResponseEntity.noContent().build();

    }


}
