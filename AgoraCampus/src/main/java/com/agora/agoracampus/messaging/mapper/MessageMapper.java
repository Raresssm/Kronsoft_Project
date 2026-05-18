package com.agora.agoracampus.messaging.mapper;

import com.agora.agoracampus.messaging.dto.response.MessageResponse;
import com.agora.agoracampus.messaging.model.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getReceiver().getId(),
                message.getContent(),
                message.getSentAt(),
                message.isAcknowledged()
        );
    }
}
