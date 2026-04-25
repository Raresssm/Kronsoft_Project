package com.agora.agoracampus.controller;

import com.agora.agoracampus.dto.CreateIndividualProfileRequest;
import com.agora.agoracampus.dto.CreateOrganizationProfileRequest;
import com.agora.agoracampus.dto.IndividualProfileResponse;
import com.agora.agoracampus.dto.OrganizationProfileResponse;
import com.agora.agoracampus.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/organizations")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationProfileResponse createOrganizationProfile(
            @Valid @RequestBody CreateOrganizationProfileRequest request
    ) {
        return profileService.createOrganizationProfile(request);
    }

    @PostMapping("/individuals")
    @ResponseStatus(HttpStatus.CREATED)
    public IndividualProfileResponse createIndividualProfile(
            @Valid @RequestBody CreateIndividualProfileRequest request
    ) {
        return profileService.createIndividualProfile(request);
    }
}
