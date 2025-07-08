package org.snomed.cis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snomed.cis.config.ImsConfig;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CookieUtilTest {

    private CookieUtil cookieUtil;
    private ImsConfig imsConfig;

    @BeforeEach
    void setup() throws Exception {
        imsConfig = mock(ImsConfig.class);
        cookieUtil = new CookieUtil();
        Field imsConfigField = CookieUtil.class.getDeclaredField("imsConfig");
        imsConfigField.setAccessible(true);
        imsConfigField.set(cookieUtil, imsConfig);
    }


    @Test
    void testFetchValue_returnsCorrectValue() {
        String input = "Token=abc123; Path=/; HttpOnly";
        String key = "Token=";
        String result = cookieUtil.fetchValue(input, key);
        assertEquals("abc123", result);
    }

    @Test
    void testUpdateDomain_replacesDomainSuccessfully() {
        String cookie = "Token=abc123; Path=/; Domain=old.com; HttpOnly";
        String result = cookieUtil.updateDomain(cookie, "new.com");
        assertTrue(result.contains("Domain=new.com"));
        assertFalse(result.contains("Domain=old.com"));
    }

    @Test
    void testFetchTokenCookie_returnsValidCookie() throws CisException {
        String cookieName = "MyToken";
        when(imsConfig.getCookieName()).thenReturn(cookieName);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "MyToken=abc123; Path=/; HttpOnly");
        ResponseEntity<String> response = new ResponseEntity<>("OK", headers, HttpStatus.OK);
        String result = cookieUtil.fetchTokenCookie(response);
        assertTrue(result.contains("MyToken=abc123"));
    }

    @Test
    void testFetchTokenCookie_throwsExceptionWhenNoCookies() {
        ResponseEntity<String> response = new ResponseEntity<>("OK", new HttpHeaders(), HttpStatus.OK);
        assertThrows(CisException.class, () -> cookieUtil.fetchTokenCookie(response));
    }

    @Test
    void testFetchTokenCookieValue_returnsTokenValue() throws CisException {
        when(imsConfig.getCookieName()).thenReturn("MyToken");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "MyToken=xyz789; Path=/; HttpOnly");
        ResponseEntity<String> response = new ResponseEntity<>("OK", headers, HttpStatus.OK);
        String tokenValue = cookieUtil.fetchTokenCookieValue(response);
        assertEquals("xyz789", tokenValue);
    }

    @Test
    void testFetchTokenCookieValue_throwsIfTokenNotFound() {
        when(imsConfig.getCookieName()).thenReturn("MissingToken");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "AnotherCookie=aaa111; Path=/;");
        ResponseEntity<String> response = new ResponseEntity<>("OK", headers, HttpStatus.OK);
        assertThrows(CisException.class, () -> cookieUtil.fetchTokenCookieValue(response));
    }

    @Test
    void testFetchValue_noSemicolon_shouldThrowException() {
        String input = "Token=abc123";
        String key = "Token=";
        assertThrows(StringIndexOutOfBoundsException.class, () -> cookieUtil.fetchValue(input, key));
    }

    @Test
    void testFetchValue_keyNotFound_shouldReturnPartialString() {
        String input = "SessionID=abc123; Path=/";
        String key = "Token=";
        String result = cookieUtil.fetchValue(input, key);
        assertEquals("onID=abc123", result);
    }

    @Test
    void testUpdateDomain_noDomainInCookie_shouldNotReplaceAnything() {
        String cookie = "Token=abc123; Path=/; HttpOnly";
        String result = cookieUtil.updateDomain(cookie, "new.com");
        assertFalse(result.contains("Domain=new.com"));
        assertEquals(cookie, result);
    }

    @Test
    void testFetchTokenCookie_multipleCookies_picksCorrectOne() throws Exception {
        when(imsConfig.getCookieName()).thenReturn("MyToken");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "AnotherCookie=aaa111; Path=/;");
        headers.add(HttpHeaders.SET_COOKIE, "MyToken=correctValue; Path=/; HttpOnly");
        headers.add(HttpHeaders.SET_COOKIE, "Extra=xyz123;");
        ResponseEntity<String> response = new ResponseEntity<>("OK", headers, HttpStatus.OK);
        String cookie = cookieUtil.fetchTokenCookie(response);
        assertTrue(cookie.contains("MyToken=correctValue"));
    }

}
