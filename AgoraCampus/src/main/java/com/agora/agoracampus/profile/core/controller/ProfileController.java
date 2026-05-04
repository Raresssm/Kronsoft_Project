package com.agora.agoracampus.profile.core.controller;

import com.agora.agoracampus.profile.core.dto.response.ProfileResponse;
import com.agora.agoracampus.profile.core.dto.request.ProfileUpdateRequest;
import com.agora.agoracampus.profile.core.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {


        private final ProfileService profileService;

    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> getProfileById(
            @PathVariable Long profileId,
            @RequestParam Long actingUserId) {
        return ResponseEntity.ok(profileService.getProfileById(profileId, actingUserId));
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long profileId,
            @RequestParam Long actingUserId,
            @RequestBody @Valid ProfileUpdateRequest dto) {
        return ResponseEntity.ok(profileService.updateProfile(profileId, actingUserId, dto));
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable Long profileId,
            @RequestParam Long actingUserId) {
        profileService.deleteProfile(profileId, actingUserId);
        return ResponseEntity.noContent().build();
    }





        /*
    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

         */









    }

