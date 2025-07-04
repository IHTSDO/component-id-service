package org.snomed.cis.controller;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.AuthenticationService;
import org.snomed.cis.util.ValidationUtil;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest(AuthenticationControllerTest.class)
class AuthenticationControllerTest {

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_success() throws CisException {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("user");
        request.setPassword("pass");

        LoginResponseDto response = LoginResponseDto.builder().token("token123").build();

        when(authenticationService.login(eq(request), any(HttpServletRequest.class))).thenReturn(ResponseEntity.ok(response));

        ResponseEntity<LoginResponseDto> result = authenticationController.login(request, httpServletRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("token123", result.getBody().getToken());
    }

    @Test
    void testLogin_failure_throwsException() throws CisException {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("user");
        request.setPassword("invalid");

        when(authenticationService.login(any(), any())).thenThrow(new CisException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        CisException ex = assertThrows(CisException.class, () -> {
            authenticationController.login(request, httpServletRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void testLoginUI_success() throws Exception {
        String formData = "username=user&password=pass";

        LoginResponseDto response = LoginResponseDto.builder().token("token123").build();

        when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(formData.getBytes())));

        when(authenticationService.login(any(LoginRequestDto.class), eq(httpServletRequest))).thenReturn(ResponseEntity.ok(response));

        ResponseEntity<LoginResponseDto> result = authenticationController.loginUI(httpServletRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("token123", result.getBody().getToken());
    }

    @Test
    void testLoginUI_invalidForm_throwsCisException() throws Exception {
        when(httpServletRequest.getInputStream()).thenThrow(new IOException("Stream read failed"));

        CisException ex = assertThrows(CisException.class, () -> {
            authenticationController.loginUI(httpServletRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("invalid form data submitted", ex.getMessage());
    }

    @Test
    void testLogout_success() throws CisException {
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto();
        logoutRequestDto.setToken("token123");

        EmptyDto emptyDto = new EmptyDto();

        when(authenticationService.logout(logoutRequestDto)).thenReturn(ResponseEntity.ok(emptyDto));

        ResponseEntity<EmptyDto> result = authenticationController.logout("token123", logoutRequestDto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testLogout_failure() throws CisException {
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto();
        logoutRequestDto.setToken("invalid");

        when(authenticationService.logout(logoutRequestDto)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid token"));

        CisException ex = assertThrows(CisException.class, () -> {
            authenticationController.logout("invalid", logoutRequestDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Invalid token", ex.getMessage());
    }

    @Test
    void testAuthenticate_success() {
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder().name("testUser").email("user@example.com").displayName("Test User").build();

        Token token = mock(Token.class);
        when(token.getAuthenticateResponseDto()).thenReturn(authDto);

        ResponseEntity<AuthenticateResponseDto> response = authenticationController.authenticate("token123", token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testUser", response.getBody().getName());
    }

    static class DelegatingServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream sourceStream;

        public DelegatingServletInputStream(ByteArrayInputStream sourceStream) {
            this.sourceStream = sourceStream;
        }

        @Override
        public int read() {
            return sourceStream.read();
        }

        @Override
        public boolean isFinished() {
            return sourceStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // Intentionally left empty: Non-blocking I/O not used in this implementation.
        }
    }

    @Test
    void testLogin_emptyUsername_shouldFailValidation() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("");
        request.setPassword("pass");

        assertThrows(CisException.class, () -> ValidationUtil.validateLoginRequestDto(request));
    }


    @Test
    void testLoginUI_onlyUsernameProvided_shouldFailValidation() throws Exception {
        String formData = "username=user";

        when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(formData.getBytes())));

        assertThrows(CisException.class, () -> {
            authenticationController.loginUI(httpServletRequest);
        });
    }

    @Test
    void testLoginUI_emptyFormData_shouldThrowValidationException() throws Exception {
        String formData = "";

        when(httpServletRequest.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(formData.getBytes())));

        assertThrows(CisException.class, () -> {
            authenticationController.loginUI(httpServletRequest);
        });
    }

    @Test
    void testLogout_tokenMismatchStillProcesses() throws CisException {
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto();
        logoutRequestDto.setToken("body-token");

        EmptyDto emptyDto = new EmptyDto();

        when(authenticationService.logout(any())).thenReturn(ResponseEntity.ok(emptyDto));

        ResponseEntity<EmptyDto> response = authenticationController.logout("query-token", logoutRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAuthenticate_withNullToken_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            authenticationController.authenticate("token123", null);
        });
    }

    @Test
    void testAuthenticate_tokenReturnsNull_shouldHandleGracefully() {
        Token token = mock(Token.class);
        when(token.getAuthenticateResponseDto()).thenReturn(null);

        ResponseEntity<AuthenticateResponseDto> response = authenticationController.authenticate("token123", token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

}
