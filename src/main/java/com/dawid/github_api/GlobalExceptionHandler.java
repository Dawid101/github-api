package com.dawid.github_api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler
    ResponseEntity<ErrorResp> handleHttpClientErrorException$NotFound(HttpClientErrorException exception) {
        ErrorResp errorResp = new ErrorResp(exception.getStatusCode().value(),exception.getStatusText());
        return new ResponseEntity<>(errorResp, HttpStatus.valueOf(exception.getStatusCode().value()));
    }

}
