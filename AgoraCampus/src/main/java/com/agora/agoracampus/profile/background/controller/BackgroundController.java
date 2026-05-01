package com.agora.agoracampus.profile.background.controller;

import com.agora.agoracampus.profile.background.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.profile.background.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.profile.background.dto.response.BackgroundResponse;
import com.agora.agoracampus.profile.background.service.BackgroundService;
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

    @GetMapping
    public ResponseEntity<List<BackgroundResponse>> getAll(
            @PathVariable Long individualProfileId) {
        return ResponseEntity.ok(backgroundService.getBackgroundsByProfileId(individualProfileId));
    }

    @GetMapping("/{backgroundId}")
    public ResponseEntity<BackgroundResponse> getById(
            @PathVariable Long backgroundId) {
        return ResponseEntity.ok(backgroundService.getBackgroundById( backgroundId));
    }

    @PostMapping
    public ResponseEntity<BackgroundResponse> create(
            @RequestBody @Valid BackgroundCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(backgroundService.createBackground( dto));
    }

    @PutMapping("/{backgroundId}")
    public ResponseEntity<BackgroundResponse> update(
            @PathVariable Long backgroundId,
            @RequestBody @Valid BackgroundUpdateRequest dto) {
        return ResponseEntity.ok(backgroundService.updateBackground(backgroundId, dto));
    }

    @DeleteMapping("/{backgroundId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long backgroundId) {
        backgroundService.deleteBackground(backgroundId);
        return ResponseEntity.noContent().build();
    }
}
