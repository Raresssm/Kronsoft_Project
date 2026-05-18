package com.agora.agoracampus.user.core.controller;

import com.agora.agoracampus.user.core.dto.response.AppUserResponse;
import com.agora.agoracampus.user.core.dto.request.CreateAppUserRequest;
import com.agora.agoracampus.user.core.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<AppUserResponse> currentUser() {
        return appUserService.findCurrentUser()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<AppUserResponse> listUsers() {
        return appUserService.findAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse createUser(@Valid @RequestBody CreateAppUserRequest request) {
        return appUserService.createUser(request);
    }
}
