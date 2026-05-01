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
    public List<ConnectionResponse> listForUser(@PathVariable Long userId) {
        return connectionService.listForUser(userId);
    }

    @GetMapping("/incoming/{receiverUserId}/pending")
    public List<ConnectionResponse> listPendingIncoming(@PathVariable Long receiverUserId) {
        return connectionService.listPendingIncoming(receiverUserId);
    }

    @GetMapping("/{id}")
    public ConnectionResponse getById(@PathVariable Long id) {
        return connectionService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ConnectionResponse> create(@Valid @RequestBody CreateConnectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(connectionService.create(request));
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
