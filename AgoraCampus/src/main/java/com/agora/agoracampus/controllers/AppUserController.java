package com.agora.agoracampus.controllers;

import com.agora.agoracampus.dto.response.AppUserResponse;
import com.agora.agoracampus.dto.request.CreateAppUserRequest;
import com.agora.agoracampus.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse createUser(@Valid @RequestBody CreateAppUserRequest request) {
        return appUserService.createUser(request);
    }
}
