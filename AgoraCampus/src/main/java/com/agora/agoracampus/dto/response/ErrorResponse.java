package com.agora.agoracampus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

public record ErrorResponse(

        int status,
        String message,
        Map<String, String> fieldErrors

) {}
