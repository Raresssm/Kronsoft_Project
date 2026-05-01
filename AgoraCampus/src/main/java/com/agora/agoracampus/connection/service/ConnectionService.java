package com.agora.agoracampus.connection.service;

import com.agora.agoracampus.connection.dto.request.CreateConnectionRequest;
import com.agora.agoracampus.connection.dto.request.UpdateConnectionStatusRequest;
import com.agora.agoracampus.connection.dto.response.ConnectionResponse;
import com.agora.agoracampus.connection.mapper.ConnectionMapper;
import com.agora.agoracampus.connection.model.Connection;
import com.agora.agoracampus.connection.model.ConnectionStatus;
import com.agora.agoracampus.connection.repository.ConnectionRepository;
import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final AppUserRepository appUserRepository;
    private final ConnectionMapper connectionMapper;

    public List<ConnectionResponse> listForUser(Long userId) {
        validateUserExists(userId);
        return connectionRepository.findAllForUser(userId).stream().map(connectionMapper::toResponse).toList();
    }

    public List<ConnectionResponse> listPendingIncoming(Long receiverUserId) {
        validateUserExists(receiverUserId);
        return connectionRepository.findByReceiver_IdAndStatus(receiverUserId, ConnectionStatus.PENDING)
                .stream()
                .map(connectionMapper::toResponse)
                .toList();
    }

    public ConnectionResponse getById(Long id) {
        return connectionMapper.toResponse(connectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Connection not found.")));
    }

    @Transactional
    public ConnectionResponse create(CreateConnectionRequest request) {
        if (request.requesterUserId().equals(request.receiverUserId())) {
            throw new BadRequestException("Users cannot connect to themselves.");
        }
        AppUser requester = validateUserExists(request.requesterUserId());
        AppUser receiver = validateUserExists(request.receiverUserId());

        connectionRepository.findBetweenUsers(request.requesterUserId(), request.receiverUserId())
                .ifPresent(c -> {
                    throw new BadRequestException("A connection request already exists between these users.");
                });

        Connection connection = Connection.builder()
                .requester(requester)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .build();

        return connectionMapper.toResponse(connectionRepository.save(connection));
    }

    @Transactional
    public ConnectionResponse updateStatus(Long connectionId, Long actingUserId, UpdateConnectionStatusRequest request) {
        Connection existing = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new NotFoundException("Connection not found."));
        ConnectionStatus status = request.status();

        switch (status) {
            case ACCEPTED, REJECTED -> {
                if (!actingUserId.equals(existing.getReceiver().getId())) {
                    throw new BadRequestException("Only the receiver can accept or reject connection requests.");
                }
                existing.setStatus(status);
            }
            case PENDING -> throw new BadRequestException("Cannot revert to PENDING via this endpoint.");
        }

        return connectionMapper.toResponse(connectionRepository.save(existing));
    }

    @Transactional
    public void delete(Long connectionId, Long actingUserId) {
        Connection existing = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new NotFoundException("Connection not found."));
        Long requesterId = existing.getRequester().getId();
        Long receiverId = existing.getReceiver().getId();
        if (!actingUserId.equals(requesterId) && !actingUserId.equals(receiverId)) {
            throw new BadRequestException("Only participants can delete this connection.");
        }
        connectionRepository.delete(existing);
    }

    private AppUser validateUserExists(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " was not found."));
    }
}
