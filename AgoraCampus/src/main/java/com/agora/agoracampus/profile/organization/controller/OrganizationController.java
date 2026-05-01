package com.agora.agoracampus.profile.organization.controller;

import com.agora.agoracampus.profile.organization.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.profile.organization.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.profile.organization.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.profile.organization.service.OrganizationProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles/")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationProfileService profileService;


    @GetMapping("organization/{id}")
    public ResponseEntity<OrganizationProfileResponse> getOrganizationProfile(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getByProfileId(id));
    }
    @PostMapping("organization")
    public ResponseEntity<OrganizationProfileResponse> createOrganizationProfile(
            @RequestBody  @Valid CreateOrganizationProfileRequest dto) {
        return ResponseEntity.ok(profileService.create(dto));
    }
    @GetMapping("organization/name/{name}")
    public ResponseEntity<List<OrganizationProfileResponse>> searchOrganizationByName(
            @PathVariable String name) {
        return ResponseEntity.ok(profileService.searchByName(name));
    }
    @GetMapping("organization/specialty/{specialties}")
    public ResponseEntity<List<OrganizationProfileResponse>> searchOrganizationBySpecialties(
            @PathVariable String specialties) {
        return ResponseEntity.ok(profileService.searchBySpecialties(specialties));
    }
    @GetMapping("organization/industry/{industry}")
    public ResponseEntity<List<OrganizationProfileResponse>> searchOrganizationByIndustry(
            @PathVariable String industry) {
        return ResponseEntity.ok(profileService.searchByIndustry(industry));
    }
    @GetMapping("organization/location/{location}")
    public ResponseEntity<List<OrganizationProfileResponse>> searchOrganizationByLocation(
            @PathVariable String location) {
        return ResponseEntity.ok(profileService.searchByLocation(location));
    }


    @PutMapping("organization/{id}")
    public ResponseEntity<OrganizationProfileResponse> updateOrganizationProfile(
            @PathVariable Long id,
            @RequestBody @Valid OrganizationProfileUpdateRequest dto
    ) {
        return ResponseEntity.ok(profileService.update(id, dto));

    }

    @DeleteMapping("organization/{id}")
    public ResponseEntity<Void> deleteOrganizationProfile(
            @PathVariable Long id) {

        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
