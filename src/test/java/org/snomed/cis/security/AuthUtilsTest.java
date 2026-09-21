package org.snomed.cis.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testGetAuthToken_whenDirectToken_returnsToken() throws CisException {
        Token token = new Token("admin", "admin", true, List.of(new SimpleGrantedAuthority("ROLE_USER")), AuthenticateResponseDto.builder().name("admin").build());
        Token result = AuthUtils.getAuthToken(token);
        assertSame(token, result);
    }

    @Test
    void testGetAuthToken_whenAnonymous_throwsCisException() {
        AnonymousAuthenticationToken anon = mock(AnonymousAuthenticationToken.class);
        CisException ex = assertThrows(CisException.class, () -> AuthUtils.getAuthToken(anon));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void testGetAuthToken_whenAuthenticatedGeneric_convertsToToken() throws CisException {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user1", "cred", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Token result = AuthUtils.getAuthToken(auth);
        assertNotNull(result);
        assertEquals("user1", result.getName());
        assertEquals("user1", result.getPrincipal());
        assertNotNull(result.getAuthenticateResponseDto());
        assertEquals("user1", result.getAuthenticateResponseDto().getName());
        assertTrue(result.getAuthenticateResponseDto().getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void testGetAuthToken_whenUnauthenticatedGeneric_throwsClassCastException() {
        Authentication unauth = mock(Authentication.class);
        when(unauth.isAuthenticated()).thenReturn(false);
        assertThrows(ClassCastException.class, () -> AuthUtils.getAuthToken(unauth));
    }

    @Test
    void testGetAuthToken_fromRequestContext_withTokenAttribute() throws CisException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Token token = new Token("reqUser", "reqUser", true, Collections.emptyList(), AuthenticateResponseDto.builder().name("reqUser").build());
        Vector<String> attrNames = new Vector<>(List.of("tokenAttr"));
        when(req.getAttributeNames()).thenReturn(attrNames.elements());
        when(req.getAttribute("tokenAttr")).thenReturn(token);

        ServletRequestAttributes attrs = new ServletRequestAttributes(req);
        RequestContextHolder.setRequestAttributes(attrs);

        Token result = AuthUtils.getAuthToken(null);
        assertSame(token, result);
    }

    @Test
    void testGetAuthToken_fromRequestContext_withSecurityContextAttribute() throws CisException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Token token = new Token("scUser", "scUser", true, Collections.emptyList(), AuthenticateResponseDto.builder().name("scUser").build());
        SecurityContext sc = new SecurityContextImpl(token);
        Vector<String> attrNames = new Vector<>(List.of("SPRING_SECURITY_CONTEXT"));
        when(req.getAttributeNames()).thenReturn(attrNames.elements());
        when(req.getAttribute("SPRING_SECURITY_CONTEXT")).thenReturn(sc);

        ServletRequestAttributes attrs = new ServletRequestAttributes(req);
        RequestContextHolder.setRequestAttributes(attrs);

        Token result = AuthUtils.getAuthToken(null);
        assertSame(token, result);
    }

    @Test
    void testGetAuthToken_fromRequestContext_withUserPrincipalToken() throws CisException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Token token = new Token("principalUser", "principalUser", true, Collections.emptyList(), AuthenticateResponseDto.builder().name("principalUser").build());
        when(req.getAttributeNames()).thenReturn(new Vector<String>().elements());
        when(req.getUserPrincipal()).thenReturn(token);

        ServletRequestAttributes attrs = new ServletRequestAttributes(req);
        RequestContextHolder.setRequestAttributes(attrs);

        Token result = AuthUtils.getAuthToken(null);
        assertSame(token, result);
    }

    @Test
    void testGetAuthToken_fromSecurityContextHolder() throws CisException {
        Token token = new Token("holderUser", "holderUser", true, Collections.emptyList(), AuthenticateResponseDto.builder().name("holderUser").build());
        SecurityContextHolder.getContext().setAuthentication(token);

        Token result = AuthUtils.getAuthToken(null);
        assertSame(token, result);
    }

    @Test
    void testGetAuthToken_whenNoAuthAvailable_throwsUnauthorized() {
        CisException ex = assertThrows(CisException.class, () -> AuthUtils.getAuthToken(null));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}
