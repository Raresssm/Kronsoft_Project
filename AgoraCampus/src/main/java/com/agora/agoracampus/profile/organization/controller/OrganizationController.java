package com.agora.agoracampus.profile.organization.controller;

import com.agora.agoracampus.profile.core.service.ProfileService;
import com.agora.agoracampus.profile.organization.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.profile.organization.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.profile.organization.dto.response.OrganizationProfileResponse;
import com.agora.agoracampus.profile.organization.service.OrganizationProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationProfileService profileService;


    @GetMapping("/{id}")
    public ResponseEntity<OrganizationProfileResponse> getOrganizationProfile(
            @PathVariable Long id,
            @RequestParam Long actingUserId) {
        return ResponseEntity.ok(profileService.getByProfileId(id, actingUserId));
    }
    @PostMapping
    public ResponseEntity<OrganizationProfileResponse> createOrganizationProfile(
            @RequestBody @Valid CreateOrganizationProfileRequest dto,
            @RequestParam Long actingUserId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.create(actingUserId, dto));
    }
    @GetMapping("/name/{name}")
    public ResponseEntity<List<OrganizationProfileResponse>> searchOrganizationByName(
            @PathVariable String name) {
        return ResponseEntity.ok(profileService.searchByName(name));
    }
    @GetMapping("/specialty/{specialties}")
    public ResponseEntity<List<OrganizationProfileResponse>> searchOrganizationBySpecialties(
            @PathVariable String specialties) {
        return ResponseEntity.ok(profileService.searchBySpecialties(specialties));
    }
    @GetMapping("/industry/{industry}")
    public ResponseEntity<List<OrganizationProfileResponse>> searchOrganizationByIndustry(
            @PathVariable String industry) {
        return ResponseEntity.ok(profileService.searchByIndustry(industry));
    }
    @GetMapping("/location/{location}")
    public ResponseEntity<List<OrganizationProfileResponse>> searchOrganizationByLocation(
            @PathVariable String location) {
        return ResponseEntity.ok(profileService.searchByLocation(location));
    }


    @PutMapping("/{id}")
    public ResponseEntity<OrganizationProfileResponse> updateOrganizationProfile(
            @PathVariable Long id,
            @RequestParam Long actingUserId,
            @RequestBody @Valid OrganizationProfileUpdateRequest dto) {
        return ResponseEntity.ok(profileService.update(id, actingUserId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganizationProfile(
            @PathVariable Long id,
            @RequestParam Long actingUserId) {

     profileService.delete(id,actingUserId);
        return ResponseEntity.noContent().build();
    }

}
