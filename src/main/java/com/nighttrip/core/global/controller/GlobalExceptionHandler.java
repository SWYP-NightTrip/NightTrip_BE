package com.nighttrip.core.global.controller;

import com.nighttrip.core.global.dto.ApiErrorResponse;
import com.nighttrip.core.global.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiErrorResponse.of(
                        ex.getStatus().name(),
                        ex.getMessage()
                ));
    }
}