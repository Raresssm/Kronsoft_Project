package com.agora.agoracampus.controllers;

import com.agora.agoracampus.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.service.OrganizationProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/")
@RequiredArgsConstructor
public class OrganizationController {

    OrganizationProfileService profileService;


    @GetMapping("organization/{profileId}")
    public ResponseEntity<OrganizationProfileResponse> getOrganizationProfile(@PathVariable Integer profileId) {
        return ResponseEntity.ok(profileService.getByProfileId(profileId));
    }
    @PostMapping("organization")
    public ResponseEntity<OrganizationProfileResponse> createOrganizationProfile(
            @RequestBody  @Valid CreateOrganizationProfileRequest dto) {
        return ResponseEntity.ok(profileService.create(dto));
    }


    @PutMapping("organization/{profileId}")
    public ResponseEntity<OrganizationProfileResponse> updateOrganizationProfile(@PathVariable Integer id, @RequestBody  @Valid OrganizationProfileUpdateRequest dto) {
        return ResponseEntity.ok(profileService.update(id,dto));

    }

    @DeleteMapping("organization/{profileId}")
    public ResponseEntity<Void> deleteOrganizationProfile(
            @PathVariable Integer profileId) {

        profileService.delete(profileId);
        return ResponseEntity.noContent().build();
    }






}
