package com.agora.agoracampus.connection.controller;

import com.agora.agoracampus.connection.dto.request.CreateConnectionRequest;
import com.agora.agoracampus.connection.dto.request.UpdateConnectionStatusRequest;
import com.agora.agoracampus.connection.dto.response.ConnectionResponse;
import com.agora.agoracampus.connection.service.ConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @GetMapping("/users/{userId}")
    public List<ConnectionResponse> listForUser(@PathVariable Long userId, @RequestParam Long actingUserId) {
        return connectionService.listForUser(userId, actingUserId);
    }

    @GetMapping("/incoming/{receiverUserId}/pending")
    public List<ConnectionResponse> listPendingIncoming(@PathVariable Long receiverUserId, @RequestParam Long actingUserId) {
        return connectionService.listPendingIncoming(receiverUserId, actingUserId);
    }

    @GetMapping("/{id}")
    public ConnectionResponse getById(@PathVariable Long id, @RequestParam Long actingUserId) {
        return connectionService.getById(id, actingUserId);
    }

    @PostMapping
    public ResponseEntity<ConnectionResponse> create(
            @RequestParam Long actingUserId,
            @Valid @RequestBody CreateConnectionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(connectionService.create(actingUserId, request));
    }

    /**
     * @param actingUserId who performs the transition (typically the receiver accepts/rejects).
     */
    @PatchMapping("/{id}/status")
    public ConnectionResponse updateStatus(
            @PathVariable Long id,
            @RequestParam Long actingUserId,
            @Valid @RequestBody UpdateConnectionStatusRequest request
    ) {
        return connectionService.updateStatus(id, actingUserId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long actingUserId) {
        connectionService.delete(id, actingUserId);
        return ResponseEntity.noContent().build();
    }
}
