package org.snomed.cis.exception;

import org.springframework.http.HttpStatus;

public class CisException extends Exception {

    private static final long serialVersionUID = 1L;

    private HttpStatus status;
    private String errorMessage;

    public CisException(HttpStatus status, String errorMessage) {
        super(errorMessage);
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}

