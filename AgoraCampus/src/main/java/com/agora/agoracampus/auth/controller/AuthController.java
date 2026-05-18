package com.agora.agoracampus.auth.controller;

import com.agora.agoracampus.auth.dto.request.RegisterRequest;
import com.agora.agoracampus.auth.service.KeycloakRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KeycloakRegistrationService keycloakRegistrationService;

    public AuthController(KeycloakRegistrationService keycloakRegistrationService) {
        this.keycloakRegistrationService = keycloakRegistrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        keycloakRegistrationService.register(request);
    }
}
