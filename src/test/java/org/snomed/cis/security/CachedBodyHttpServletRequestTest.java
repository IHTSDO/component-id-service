package org.snomed.cis.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CachedBodyHttpServletRequestTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() throws IOException {
        mockRequest = mock(HttpServletRequest.class);
        byte[] body = "test-body".getBytes();
        when(mockRequest.getInputStream()).thenAnswer(invocation -> new ServletInputStream() {
            private final ByteArrayInputStream buffer = new ByteArrayInputStream(body);

            @Override
            public int read() throws IOException {
                return buffer.read();
            }

            @Override
            public boolean isFinished() {
                return buffer.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException("Non-blocking IO is not supported.");
            }

        });
    }

    @Test
    void testMultipleReads_shouldReturnSameContent() throws IOException {
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(mockRequest);

        String firstRead = new BufferedReader(new InputStreamReader(cachedRequest.getInputStream()))
                .lines().reduce("", String::concat);

        String secondRead = new BufferedReader(new InputStreamReader(cachedRequest.getInputStream()))
                .lines().reduce("", String::concat);

        assertEquals(firstRead, secondRead);
    }
    @BeforeEach
    void setupEmptyBody() throws IOException {
        mockRequest = mock(HttpServletRequest.class);

        ServletInputStream servletInputStream = new ServletInputStream() {
            public boolean isFinished() { return true; }
            public boolean isReady() { return true; }
            public void setReadListener(ReadListener listener) {throw new UnsupportedOperationException("Non-blocking IO is not supported.");}
            public int read() { return -1; }
        };

        when(mockRequest.getInputStream()).thenReturn(servletInputStream);
    }

    @Test
    void testEmptyBody_shouldReturnEmptyString() throws IOException {
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(mockRequest);
        String body = new BufferedReader(new InputStreamReader(cachedRequest.getInputStream()))
                .lines().reduce("", String::concat);

        assertEquals("", body);
    }
    @Test
    void testIOExceptionDuringConstruction_shouldFailGracefully() throws IOException {
        HttpServletRequest badRequest = mock(HttpServletRequest.class);

        when(badRequest.getInputStream()).thenThrow(new IOException("Stream broken"));

        assertThrows(IOException.class, () -> new CachedBodyHttpServletRequest(badRequest));
    }

}
