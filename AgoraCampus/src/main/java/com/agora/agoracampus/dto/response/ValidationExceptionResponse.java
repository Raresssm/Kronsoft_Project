package com.agora.agoracampus.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationExceptionResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private Map<String, String> validationErrors;
    private String path;
}
