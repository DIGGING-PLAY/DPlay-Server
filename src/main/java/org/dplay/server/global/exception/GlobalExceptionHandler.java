package org.dplay.server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.dplay.server.global.response.ResponseError;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(DPlayException.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(DPlayException e) {
        log.error("DPlayException occurred", e);
        return ResponseBuilder.error(e.getResponseError());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpClientErrorException(HttpClientErrorException e) {
        log.error("HttpClientErrorException occurred: {}", e.getMessage());

        String responseBody = e.getResponseBodyAsString();

        if (responseBody.contains("KOE320")) {
            return ResponseBuilder.error(ResponseError.INVALID_GRANT);
        } else if (responseBody.contains("401")) {
            return ResponseBuilder.error(ResponseError.INVALID_ACCESS_TOKEN);
        }

        return ResponseBuilder.error(ResponseError.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException occurred", e);
        String fieldName = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getField)
                .orElse("");

        if (fieldName.equals("year") || fieldName.equals("month")) {
            return ResponseBuilder.error(ResponseError.INVALID_DATE_TYPE);
        }

        return ResponseBuilder.error(ResponseError.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException occurred", e);
        return ResponseBuilder.error(ResponseError.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException occurred", e);
        return ResponseBuilder.error(ResponseError.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Exception occurred", e);
        return ResponseBuilder.error(ResponseError.INTERNAL_SERVER_ERROR);
    }
}
