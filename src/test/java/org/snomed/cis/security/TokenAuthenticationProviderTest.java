package org.snomed.cis.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.snomed.cis.dto.AuthenticateRequestDto;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenAuthenticationProviderTest {

    @InjectMocks
    private TokenAuthenticationProvider provider;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticate_validToken_shouldReturnAuthenticatedToken() throws Exception {
        String token = "valid-token";
        AuthenticateResponseDto dto = mock(AuthenticateResponseDto.class);
        when(dto.getName()).thenReturn("TestUser");
        when(dto.getRoles()).thenReturn(List.of("ROLE_USER"));
        when(authenticationService.authenticate(any(AuthenticateRequestDto.class))).thenReturn(ResponseEntity.ok(dto));
        Token authToken = new Token(token, "TestUser", false, List.of(), dto);
        Field publicField = Token.class.getDeclaredField("isPublicEndpoint");
        publicField.setAccessible(true);
        publicField.set(authToken, false);
        Authentication result = provider.authenticate(authToken);
        assertTrue(result.isAuthenticated());
        assertEquals(token, result.getPrincipal());
        assertEquals("TestUser", ((Token) result).getUserName());
    }

    @Test
    void testAuthenticate_publicEndpointWithNoToken_shouldReturnUnAuthenticatedToken() {
        Token token = new Token(true);
        Authentication result = provider.authenticate(token);
        assertFalse(result.isAuthenticated());
        assertTrue(((Token) result).getIsPublicEndpoint());
    }

    @Test
    void testAuthenticate_nullToken_shouldThrowException() {
        Token token = new Token(null, false);
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            provider.authenticate(token);
        });
    }

    @Test
    void testAuthenticate_cisExceptionInternalServer_shouldThrowAuthServiceException() throws CisException {
        String token = "server-error";
        CisException ex = new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
        when(authenticationService.authenticate(any())).thenThrow(ex);
        Token tokenObj = new Token(token, false);
        assertThrows(AuthenticationServiceException.class, () -> {
            provider.authenticate(tokenObj);
        });
    }

    @Test
    void testAuthenticate_cisExceptionBadRequest_shouldThrowBadCredentials() throws CisException {
        String token = "bad-token";
        CisException ex = new CisException(HttpStatus.BAD_REQUEST, "Invalid token");
        when(authenticationService.authenticate(any())).thenThrow(ex);
        Token tokenObj = new Token(token, false);
        assertThrows(BadCredentialsException.class, () -> {
            provider.authenticate(tokenObj);
        });
    }

    @Test
    void testSupports_shouldReturnTrueForToken() {
        assertTrue(provider.supports(Token.class));
    }

    @Test
    void testSupports_shouldReturnFalseForOtherClass() {
        assertFalse(provider.supports(Authentication.class));
    }
}
