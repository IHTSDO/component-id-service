package org.snomed.cis.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErrorResponseTest {

    @Test
    void testBuilder_shouldSetAllFields() {
        ErrorResponse response = ErrorResponse.builder()
                .statusCode(404)
                .message("Not Found")
                .build();

        assertEquals(404, response.getStatusCode());
        assertEquals("Not Found", response.getMessage());
    }

    @Test
    void testBuilder_withNullValues_shouldHandleGracefully() {
        ErrorResponse response = ErrorResponse.builder()
                .statusCode(null)
                .message(null)
                .build();

        assertNull(response.getStatusCode());
        assertNull(response.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        ErrorResponse response = ErrorResponse.builder().build();

        response.setStatusCode(500);
        response.setMessage("Internal Server Error");

        assertEquals(500, response.getStatusCode());
        assertEquals("Internal Server Error", response.getMessage());
    }
}
