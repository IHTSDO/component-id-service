package org.snomed.cis.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    @Test
    void testToken_withMinimalFields() {
        String tokenStr = "abc123";
        String userName = "john";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Token token = new Token(tokenStr, userName, true, authorities, null);
        assertEquals(tokenStr, token.getToken());
        assertEquals(userName, token.getUserName());
        assertNull(token.getAuthenticateResponseDto());
        assertTrue(token.isAuthenticated());
    }

    @Test
    void testToken_withTokenOnlyConstructor() {
        String tokenStr = "xyz789";
        Token token = new Token(tokenStr, true);
        assertEquals(tokenStr, token.getToken());
        assertFalse(token.isAuthenticated());
        assertTrue(token.getIsPublicEndpoint());
        assertEquals(tokenStr, token.getPrincipal());
    }

    @Test
    void testToken_withPublicEndpointOnlyConstructor() {
        Token token = new Token(false);
        assertFalse(token.isAuthenticated());
        assertFalse(token.getIsPublicEndpoint());
        assertNull(token.getPrincipal());
        assertNull(token.getCredentials());
    }
}
