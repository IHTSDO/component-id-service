package org.snomed.cis.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CisException.class)
    public ResponseEntity<?> handleCisException(CisException exception) {
        logger.error("cis exception :: ", exception);
        return new ResponseEntity<>(ErrorResponse.builder().statusCode(exception.getStatus().value()).message(exception.getErrorMessage()).build(), exception.getStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        logger.error("input validation failed :: ", exception);
        return new ResponseEntity<>(ErrorResponse.builder().statusCode(HttpStatus.BAD_REQUEST.value()).message(Objects.requireNonNull(exception.getFieldError()).getDefaultMessage()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception) {
        logger.error("exception thrown :: ", exception);
        ResponseEntity<?> response;
        if(exception instanceof IllegalArgumentException){
            response = new ResponseEntity<>(ErrorResponse.builder().statusCode(HttpStatus.BAD_REQUEST.value()).message(exception.toString()).build(), HttpStatus.BAD_REQUEST);
        }else{
            response = new ResponseEntity<>(ErrorResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).message(exception.toString()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

}
