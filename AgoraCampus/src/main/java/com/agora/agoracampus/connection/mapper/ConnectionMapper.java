package com.agora.agoracampus.connection.mapper;

import com.agora.agoracampus.connection.dto.response.ConnectionResponse;
import com.agora.agoracampus.connection.model.Connection;
import org.springframework.stereotype.Component;

@Component
public class ConnectionMapper {

    public ConnectionResponse toResponse(Connection connection) {
        return new ConnectionResponse(
                connection.getId(),
                connection.getRequester().getId(),
                connection.getReceiver().getId(),
                connection.getStatus(),
                connection.getCreatedAt()
        );
    }
}
