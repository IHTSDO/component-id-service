package org.snomed.cis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CisException.class)
    public ResponseEntity<?> handleCisException(CisException exception) {
        return new ResponseEntity<>(ErrorResponse.builder().message(exception.getErrorMessage()).build(), exception.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception) {
        return new ResponseEntity<>(ErrorResponse.builder().message(exception.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
