package com.agora.agoracampus.profile.organization.controller;

import com.agora.agoracampus.profile.organization.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.profile.organization.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.profile.organization.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.profile.organization.service.OrganizationProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationProfileService profileService;


    @GetMapping("organization/{profileId}")
    public ResponseEntity<OrganizationProfileResponse> getOrganizationProfile(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.getByProfileId(profileId));
    }
    @PostMapping("organization")
    public ResponseEntity<OrganizationProfileResponse> createOrganizationProfile(
            @RequestBody  @Valid CreateOrganizationProfileRequest dto) {
        return ResponseEntity.ok(profileService.create(dto));
    }


    @PutMapping("organization/{profileId}")
    public ResponseEntity<OrganizationProfileResponse> updateOrganizationProfile(
            @PathVariable Long profileId,
            @RequestBody @Valid OrganizationProfileUpdateRequest dto
    ) {
        return ResponseEntity.ok(profileService.update(profileId, dto));

    }

    @DeleteMapping("organization/{profileId}")
    public ResponseEntity<Void> deleteOrganizationProfile(
            @PathVariable Long profileId) {

        profileService.delete(profileId);
        return ResponseEntity.noContent().build();
    }






}
