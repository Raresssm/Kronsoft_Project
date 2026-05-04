package com.agora.agoracampus.profile.individual.controller;

import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileCreateRequest;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileUpdateRequest;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import com.agora.agoracampus.profile.individual.service.IndividualProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/individuals")
@RequiredArgsConstructor
public class IndividualController {

    private final IndividualProfileService individualProfileService;


    @GetMapping("/title/{title}")
    public ResponseEntity<List<IndividualProfileResponse>> getByRole(
            @Valid   @PathVariable  String title) {
        return ResponseEntity.ok(
                individualProfileService.getByTitle(title));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<IndividualProfileResponse> getByProfileId(
            @PathVariable Long profileId,
            @RequestParam Long actingUserId) {
        return ResponseEntity.ok(
                individualProfileService.getByProfileId(profileId, actingUserId));
    }

    @PostMapping
    public ResponseEntity<IndividualProfileResponse> create(
            @RequestParam Long actingUserId,
            @RequestBody @Valid IndividualProfileCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(individualProfileService.create(actingUserId, dto));
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<IndividualProfileResponse> update(
            @PathVariable Long profileId,
            @RequestParam Long actingUserId,
            @RequestBody @Valid IndividualProfileUpdateRequest dto) {
        return ResponseEntity.ok(
                individualProfileService.update(profileId, actingUserId, dto));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long profileId,
            @RequestParam Long actingUserId) {
        individualProfileService.delete(profileId, actingUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<IndividualProfileResponse>> searchByName(
            @PathVariable  String name) {
        return ResponseEntity.ok(individualProfileService.getProfileByName(name));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<IndividualProfileResponse>> searchByLocation(
            @PathVariable String location) {
        return ResponseEntity.ok(individualProfileService.getProfileByLocation(location));
    }


}