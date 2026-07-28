package org.snomed.cis.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;

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
        if (isBrokenPipeException(exception)) {
            logger.warn("Broken Pipe Detected, client has disconnected");
            return null;
        }
        logger.error("exception thrown :: ", exception);
        ResponseEntity<?> response;
        if(exception instanceof IllegalArgumentException){
            response = new ResponseEntity<>(ErrorResponse.builder().statusCode(HttpStatus.BAD_REQUEST.value()).message(exception.toString()).build(), HttpStatus.BAD_REQUEST);
        }else{
            response = new ResponseEntity<>(ErrorResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).message(exception.toString()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private boolean isBrokenPipeException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        String className = throwable.getClass().getName();
        if (className.equals("org.apache.catalina.connector.ClientAbortException") ||
            className.equals("org.springframework.web.context.request.async.AsyncRequestNotUsableException")) {
            return true;
        }
        String message = throwable.getMessage();
        if (message != null && (message.toLowerCase().contains("broken pipe") || message.toLowerCase().contains("connection reset"))) {
            return true;
        }
        return isBrokenPipeException(throwable.getCause());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        logger.error("missing request parameter :: ", exception);
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        logger.error("AccessDeniedException: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(403, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(400, "Invalid scheme name: " + ex.getValue())
        );
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Invalid or missing request body"));
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Request method '" + ex.getMethod() + "' is not supported");

        error.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        error.setMessage("Request method '" + ex.getMethod() + "' is not supported");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }


}
