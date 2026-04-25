package com.agora.agoracampus.exception;

import com.agora.agoracampus.dto.response.ErrorResponse;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, ex.getMessage(), null));
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400,ex.getMessage(), null));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
               Map<String, String> fieldErrors = new HashMap<>();
                ex.getBindingResult().getFieldErrors().forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400,"Validation failed", fieldErrors));
        }
    }



