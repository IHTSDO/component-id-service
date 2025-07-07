package org.snomed.cis.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class CisExceptionTest {

    @Test
    void testCisExceptionInitialization() {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = "Invalid request";
        CisException exception = new CisException(status, errorMessage);
        assertEquals(status, exception.getStatus());
        assertEquals(errorMessage, exception.getErrorMessage());
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testCisExceptionWithNullValues() {
        CisException exception = new CisException(null, null);
        assertNull(exception.getStatus());
        assertNull(exception.getErrorMessage());
        assertNull(exception.getMessage());
    }

    @Test
    void testCisExceptionIsInstanceOfException() {
        CisException exception = new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error");
        assertTrue(exception instanceof Exception);
    }
}
