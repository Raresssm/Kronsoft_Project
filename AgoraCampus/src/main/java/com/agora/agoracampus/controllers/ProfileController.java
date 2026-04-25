package com.agora.agoracampus.controllers;

import com.agora.agoracampus.dto.response.ProfileResponse;
import com.agora.agoracampus.dto.request.ProfileUpdateRequest;
import com.agora.agoracampus.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {


        private final ProfileService profileService;


        @GetMapping("/{id}")
        public ResponseEntity<ProfileResponse> getProfileById(@PathVariable Long id) {
            return ResponseEntity.ok(profileService.getProfileById(id));
        }




        @PutMapping("/{id}")
        public ResponseEntity<ProfileResponse> updateProfile(@PathVariable Long id,
                                                     @RequestBody  @Valid ProfileUpdateRequest dto) {
            return ResponseEntity.ok(profileService.updateProfile(id, dto));
        }

        @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id){
            profileService.deleteProfile(id);
            return ResponseEntity.noContent().build();
        }



        /*
    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

         */









    }

