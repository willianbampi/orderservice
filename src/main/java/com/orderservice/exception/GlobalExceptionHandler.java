package com.orderservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String VALIDATION_ERROR_LOG = "Validation error: {}";
    private static final String UNEXPECTED_ERROR_LOG = "Unexpected error";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";
    private static final String HANDLED_ERROR_LOG = "Handled error: {}";

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFoundException(OrderNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity(NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PartnerNotFoundException.class)
    public ResponseEntity<ApiError> handlePartnerNotFoundException(PartnerNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity(NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExistsException(AlreadyExistsException ex, HttpServletRequest request) {
        return buildResponseEntity(NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientCreditException.class)
    public ResponseEntity<ApiError> handleInsuficientCreditException(InsufficientCreditException ex, HttpServletRequest request) {
        return buildResponseEntity(BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> ApiError.FieldError.builder()
                        .field(err.getField())
                        .message(err.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ApiError apiError = buildApiError(BAD_REQUEST, VALIDATION_FAILED, request, fieldErrors);
        log.warn(VALIDATION_ERROR_LOG, apiError);
        return new ResponseEntity<>(apiError, BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllUnhandled(Exception ex, HttpServletRequest request) {
        log.error(UNEXPECTED_ERROR_LOG, ex);
        return buildResponseEntity(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE, request);
    }

    private ResponseEntity<ApiError> buildResponseEntity(HttpStatus status, String message, HttpServletRequest request) {
        ApiError apiError = buildApiError(status, message, request, null);
        log.warn(HANDLED_ERROR_LOG, apiError);
        return new ResponseEntity<>(apiError, status);
    }

    private ApiError buildApiError(HttpStatus status, String message, HttpServletRequest request, @Nullable List<ApiError.FieldError> fieldErrors) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                //.traceId(getTraceId())
                .fieldErrors(fieldErrors)
                .build();
    }

    /**
    private String getTraceId() {
        return java.util.Optional.ofNullable(
                        org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration.getCurrentTraceContext())
                .map(ctx -> ctx.context().map(c -> c.traceId()).orElse("N/A"))
                .orElse("N/A");
    }
     */

}