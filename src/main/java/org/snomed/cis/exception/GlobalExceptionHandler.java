package org.snomed.cis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CisException.class)
    public ResponseEntity<?> handleCisException(CisException exception) {
        return new ResponseEntity<>(ErrorResponse.builder().statusCode(exception.getStatus().value()).message(exception.getErrorMessage()).build(), exception.getStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return new ResponseEntity<>(ErrorResponse.builder().statusCode(HttpStatus.BAD_REQUEST.value()).message(exception.getFieldError().getDefaultMessage()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception) {
        return new ResponseEntity<>(ErrorResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).message(exception.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
