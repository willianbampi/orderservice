package com.orderservice.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ApiError {

    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private String traceId;
    private List<FieldError> fieldErrors;

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String message;
    }

}
