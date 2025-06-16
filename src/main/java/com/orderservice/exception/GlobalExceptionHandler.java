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

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFoundException(OrderNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity(NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PartnerNotFoundException.class)
    public ResponseEntity<ApiError> handlePartnerNotFoundException(PartnerNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity(NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InsuficientCreditException.class)
    public ResponseEntity<ApiError> handleInsuficientCreditException(InsuficientCreditException ex, HttpServletRequest request) {
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

        ApiError apiError = buildApiError(BAD_REQUEST, "Validation failed", request, fieldErrors);

        log.warn("Validation error: {}", apiError);

        return new ResponseEntity<>(apiError, BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return buildResponseEntity(INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    private ResponseEntity<ApiError> buildResponseEntity(HttpStatus status, String message, HttpServletRequest request) {
        ApiError apiError = buildApiError(status, message, request, null);
        log.warn("Handled error: {}", apiError);
        return new ResponseEntity<>(apiError, status);
    }

    private ApiError buildApiError(HttpStatus status, String message, HttpServletRequest request, @Nullable List<ApiError.FieldError> fieldErrors) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .fieldErrors(fieldErrors)
                .build();
    }

    private String getTraceId() {
        return java.util.Optional.ofNullable(
                        org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration.getCurrentTraceContext())
                .map(ctx -> ctx.context().map(c -> c.traceId()).orElse("N/A"))
                .orElse("N/A");
    }

}
