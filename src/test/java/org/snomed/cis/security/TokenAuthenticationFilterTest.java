package org.snomed.cis.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TokenAuthenticationFilterTest {

    private TokenAuthenticationFilter filter;
    private AuthenticationManager authenticationManager;
    private HttpServletRequest request;
    private HttpServletResponse response;


    @BeforeEach
    void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filter = new TokenAuthenticationFilter(authenticationManager, "ims-cookie");
        String jsonBody = "{\"token\":\"abc123\"}";
        ByteArrayInputStream byteStream = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8));
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        filter = new TokenAuthenticationFilter(authenticationManager, "ims-cookie");
        when(authenticationManager.authenticate(any())).thenReturn(Mockito.mock(Authentication.class));
        ServletInputStream servletInputStream = new DelegatingServletInputStream(byteStream);
        when(request.getInputStream()).thenReturn(servletInputStream);
    }

    @Test
    void testMockedRequestWithJsonBody() throws Exception {
        HttpServletRequest requests = mock(HttpServletRequest.class);
        String jsonBody = "{\"token\":\"abc123\"}";
        ByteArrayInputStream byteStream = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8));
        when(requests.getInputStream()).thenReturn(new DelegatingServletInputStream(byteStream));
        ServletInputStream stream = requests.getInputStream();
        String result = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(result.contains("abc123"));
    }

    @Test
    void testTokenFromFormEncodedBody() throws Exception {
        when(request.getRequestURI()).thenReturn("/authenticate");
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream("token=abc123".getBytes(StandardCharsets.UTF_8))));
        Authentication expectedAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(expectedAuth);
        Authentication actualAuth = filter.attemptAuthentication(request, response);
        assertNotNull(actualAuth);
    }

    @Test
    void testTokenFromQueryParam() {
        when(request.getRequestURI()).thenReturn("/scheme/generate");
        when(request.getParameter("token")).thenReturn("abc123");
        Authentication expectedAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(expectedAuth);
        Authentication actualAuth = filter.attemptAuthentication(request, response);
        assertNotNull(actualAuth);
    }

    @Test
    void testTokenFromCookie() throws Exception {
        when(request.getRequestURI()).thenReturn("/sct/namespaces");
        when(request.getHeader("cookie")).thenReturn("ts-author={\"token\":\"abc123\"}");
        Authentication expectedAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(expectedAuth);
        Authentication actualAuth = filter.attemptAuthentication(request, response);
        assertNotNull(actualAuth);
    }

    @Test
    void testMissingTokenForPublicEndpoint() {
        when(request.getRequestURI()).thenReturn("/sct/namespaces");
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        Authentication expectedAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(expectedAuth);
        Authentication auth = filter.attemptAuthentication(request, response);
        assertNotNull(auth);
    }

    @Test
    void testInvalidJsonBodyFallback() throws Exception {
        when(request.getRequestURI()).thenReturn("/authenticate");
        String invalidJson = "bad-json";
        InputStream stream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(stream));
        when(request.getParameter("token")).thenReturn("abc123");
        Authentication expectedAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(expectedAuth);
        Authentication auth = filter.attemptAuthentication(request, response);
        assertNotNull(auth);
    }

    @Test
    void testUnsuccessfulAuthentication() throws IOException {
        AuthenticationException authException = new AuthenticationException("Invalid") {
        };
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
        filter.unsuccessfulAuthentication(request, response, authException);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");
        verify(outputStream).println(contains("Invalid"));
    }

    @Test
    void testSuccessfulAuthentication() throws Exception {
        Authentication auth = mock(Authentication.class);
        FilterChain mockFilterChain = mock(FilterChain.class);
        filter.successfulAuthentication(request, response, mockFilterChain, auth);
        verify(mockFilterChain).doFilter(request, response);
    }


    static class DelegatingServletInputStream extends ServletInputStream {
        private final InputStream inputStream;

        public DelegatingServletInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public boolean isFinished() {
            try {
                return inputStream.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener listener) {
            throw new UnsupportedOperationException("Async data reading is not supported.");
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }

}
