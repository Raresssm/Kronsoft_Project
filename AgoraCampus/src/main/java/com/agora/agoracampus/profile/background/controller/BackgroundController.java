package com.agora.agoracampus.profile.background.controller;

import com.agora.agoracampus.profile.background.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.profile.background.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.profile.background.dto.response.BackgroundResponse;
import com.agora.agoracampus.profile.background.service.BackgroundService;
import com.agora.agoracampus.profile.individual.dto.response.IndividualProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/individuals/{individualProfileId}/backgrounds")
@RequiredArgsConstructor

public class BackgroundController {

    private final BackgroundService backgroundService;


    @PostMapping
    public ResponseEntity<BackgroundResponse >create(
            @PathVariable Long individualProfileId,
            @RequestParam Long actingUserId,
            @RequestBody @Valid BackgroundCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(backgroundService.createBackground(individualProfileId, actingUserId, dto));
    }

    @PutMapping("/{backgroundId}")
    public ResponseEntity<BackgroundResponse > update(
            @PathVariable Long individualProfileId,
            @PathVariable Long backgroundId,
            @RequestParam Long actingUserId,
            @RequestBody @Valid BackgroundUpdateRequest dto) {
        return ResponseEntity.ok(
                backgroundService.updateBackground(individualProfileId, backgroundId, actingUserId, dto));
    }

    @DeleteMapping("/{backgroundId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long individualProfileId,
            @PathVariable Long backgroundId,
            @RequestParam Long actingUserId) {
       backgroundService.deleteBackground(individualProfileId, backgroundId, actingUserId);

       return ResponseEntity.noContent().build();

    }
    }

