package com.agora.agoracampus.dto.response;

import java.time.LocalDateTime;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ErrorResponseClass {

        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;


}
