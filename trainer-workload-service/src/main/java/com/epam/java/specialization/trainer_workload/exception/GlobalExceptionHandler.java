package com.epam.java.specialization.trainer_workload.exception;

import com.epam.java.specialization.trainer_workload.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation error in workload service: {}", errors);
        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request payload")
                .validationErrors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(InvalidWorkloadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidWorkloadRequest(InvalidWorkloadRequestException ex) {
        log.warn("Business validation error: {}", ex.getMessage());
        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found in workload service: {}", ex.getMessage());
        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(new Date())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDto> handleDatabaseException(DataAccessException ex) {
        log.error("MongoDB data access error: ", ex);
        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(new Date())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Database Error")
                .message("Failed to execute database operation")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        log.error("Unexpected error in workload service: ", ex);
        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(new Date())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred in workload service")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }
}