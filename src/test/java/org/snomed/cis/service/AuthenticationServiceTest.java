package org.snomed.cis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.config.ImsConfig;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.util.CookieUtil;
import org.snomed.cis.util.RequestManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    private final ImsConfig imsConfig = mock(ImsConfig.class);
    private final RequestManager requestManager = mock(RequestManager.class);
    private final CookieUtil cookieUtil = mock(CookieUtil.class);
    private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    private final AuthenticationService authenticationService = new AuthenticationService();

    @BeforeEach
    void setUp() throws Exception {
        injectPrivateField("imsConfig", imsConfig);
        injectPrivateField("requestManager", requestManager);
        injectPrivateField("cookieUtil", cookieUtil);
    }

    private void injectPrivateField(String fieldName, Object value) throws Exception {
        Field field = AuthenticationService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(authenticationService, value);
    }

    @Test
    void testLogin_success() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setUsername("user");
        requestDto.setPassword("pass");
        String imsUrl = "https://ims/login";
        String token = "dummy-token";
        String payload = new JSONObject().put("login", "user").put("password", "pass").toString();
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLoginUrl()).thenReturn("/login");
        when(requestManager.postRequest(eq(imsUrl), isNull(), eq(payload))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
        when(cookieUtil.fetchTokenCookieValue(any())).thenReturn(token);
        ResponseEntity<LoginResponseDto> response = authenticationService.login(requestDto, httpServletRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, response.getBody().getToken());
    }

    @Test
    void testLogin_invalidCredentials_shouldThrowUnauthorized() throws CisException, JSONException {
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setUsername("user");
        requestDto.setPassword("pass");
        String imsUrl = "https://ims/login";
        String payload = new JSONObject().put("login", "user").put("password", "pass").toString();
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLoginUrl()).thenReturn("/login");
        when(requestManager.postRequest(eq(imsUrl), isNull(), eq(payload))).thenThrow(new CisException(HttpStatus.UNAUTHORIZED, "401"));
        CisException ex = assertThrows(CisException.class, () -> {
            authenticationService.login(requestDto, httpServletRequest);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getErrorMessage().contains("username/password incorrect"));
    }

    @Test
    void testLogin_imsServerError_shouldThrowException() throws CisException, JSONException {
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setUsername("user");
        requestDto.setPassword("pass");
        String payload = new JSONObject().put("login", "user").put("password", "pass").toString();
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLoginUrl()).thenReturn("/login");
        when(requestManager.postRequest(anyString(), any(), eq(payload))).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "IMS down"));
        CisException ex = assertThrows(CisException.class, () -> {
            authenticationService.login(requestDto, httpServletRequest);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        assertEquals("IMS down", ex.getErrorMessage());
    }

    @Test
    void testLogin_unknownError_shouldThrowException() throws CisException, JSONException {
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setUsername("user");
        requestDto.setPassword("pass");
        String payload = new JSONObject().put("login", "user").put("password", "pass").toString();
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLoginUrl()).thenReturn("/login");
        when(requestManager.postRequest(anyString(), any(), eq(payload))).thenThrow(new CisException(HttpStatus.UNAUTHORIZED, "Strange error"));
        CisException ex = assertThrows(CisException.class, () -> {
            authenticationService.login(requestDto, httpServletRequest);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("username/password incorrect for user input 'pa**'", ex.getErrorMessage());
    }

    @Test
    void testLogin_missingTokenInCookie_shouldReturnNullToken() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setUsername("user");
        requestDto.setPassword("pass");
        String imsUrl = "https://ims/login";
        String payload = new JSONObject().put("login", "user").put("password", "pass").toString();
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLoginUrl()).thenReturn("/login");
        when(requestManager.postRequest(eq(imsUrl), isNull(), eq(payload))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
        when(cookieUtil.fetchTokenCookieValue(any())).thenReturn(null);
        ResponseEntity<LoginResponseDto> response = authenticationService.login(requestDto, httpServletRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getToken());
    }

    @Test
    void testLogout_success() throws Exception {
        LogoutRequestDto dto = new LogoutRequestDto();
        dto.setToken("dummy");
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLogoutUrl()).thenReturn("/logout");
        when(requestManager.postRequestWithoutPayload(anyString(), any())).thenReturn(ResponseEntity.ok(""));
        ResponseEntity<EmptyDto> response = authenticationService.logout(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testLogout_invalidToken_shouldThrowUnauthorized() throws Exception {
        LogoutRequestDto dto = new LogoutRequestDto();
        dto.setToken("invalid-token");
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLogoutUrl()).thenReturn("/logout");
        CisException exception = new CisException(HttpStatus.UNAUTHORIZED, "Invalid token");
        when(requestManager.postRequestWithoutPayload(anyString(), any())).thenThrow(exception);
        CisException ex = assertThrows(CisException.class, () -> {
            authenticationService.logout(dto);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Invalid token", ex.getErrorMessage());
    }

    @Test
    void testLogout_serverError_shouldThrowException() throws Exception {
        LogoutRequestDto dto = new LogoutRequestDto();
        dto.setToken("dummy");
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLogoutUrl()).thenReturn("/logout");
        CisException exception = new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "IMS failure");
        when(requestManager.postRequestWithoutPayload(anyString(), any())).thenThrow(exception);
        CisException ex = assertThrows(CisException.class, () -> {
            authenticationService.logout(dto);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        assertEquals("IMS failure", ex.getErrorMessage());
    }

    @Test
    void testLogout_nullToken_shouldThrowException() throws Exception {
        LogoutRequestDto dto = new LogoutRequestDto();
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getLogoutUrl()).thenReturn("/logout");
        when(requestManager.postRequestWithoutPayload(anyString(), any())).thenThrow(new NullPointerException("Token is null"));
        assertThrows(NullPointerException.class, () -> {
            authenticationService.logout(dto);
        });
    }

    @Test
    void testAuthenticate_success() throws Exception {
        AuthenticateRequestDto dto = new AuthenticateRequestDto();
        dto.setToken("valid-token");
        ImsGetAccountResponseDto imsDto = new ImsGetAccountResponseDto();
        imsDto.setLogin("user");
        imsDto.setEmail("user@example.com");
        imsDto.setFirstName("First");
        imsDto.setLastName("Last");
        imsDto.setLangKey("en");
        imsDto.setRoles(List.of("ROLE_USER"));
        ObjectMapper objectMapper = new ObjectMapper();
        String responseBody = objectMapper.writeValueAsString(imsDto);
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getAuthenticateUrl()).thenReturn("/auth");
        when(requestManager.getRequest(anyString(), any())).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        ResponseEntity<AuthenticateResponseDto> result = authenticationService.authenticate(dto);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("user", result.getBody().getDisplayName());
        assertEquals("ROLE_USER", result.getBody().getRoles().get(0));
    }

    @Test
    void testAuthenticate_invalidToken_shouldThrowUnauthorized() throws CisException {
        AuthenticateRequestDto dto = new AuthenticateRequestDto();
        dto.setToken("invalid-token");
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getAuthenticateUrl()).thenReturn("/auth");
        CisException imsException = new CisException(HttpStatus.UNAUTHORIZED, "invalid token");
        when(requestManager.getRequest(anyString(), any())).thenThrow(imsException);
        CisException ex = assertThrows(CisException.class, () -> authenticationService.authenticate(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getErrorMessage().toLowerCase().contains("invalid"));
    }

    @Test
    void testAuthenticate_serverError_shouldThrowCisException() throws CisException {
        AuthenticateRequestDto dto = new AuthenticateRequestDto();
        dto.setToken("valid-token");
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getAuthenticateUrl()).thenReturn("/auth");
        CisException imsException = new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "IMS error");
        when(requestManager.getRequest(anyString(), any())).thenThrow(imsException);
        CisException ex = assertThrows(CisException.class, () -> authenticationService.authenticate(dto));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        assertEquals("unknown error", ex.getErrorMessage());
    }

    @Test
    void testAuthenticate_invalidJson_shouldThrowException() throws CisException {
        AuthenticateRequestDto dto = new AuthenticateRequestDto();
        dto.setToken("valid-token");
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getAuthenticateUrl()).thenReturn("/auth");
        when(requestManager.getRequest(anyString(), any())).thenReturn(new ResponseEntity<>("invalid-json", HttpStatus.OK));
        CisException ex = assertThrows(CisException.class, () -> authenticationService.authenticate(dto));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        assertTrue(ex.getErrorMessage().contains("parsing ims get account response"));
    }

    @Test
    void testAuthenticate_successWithNoRoles() throws Exception {
        AuthenticateRequestDto dto = new AuthenticateRequestDto();
        dto.setToken("valid-token");
        ImsGetAccountResponseDto imsDto = new ImsGetAccountResponseDto();
        imsDto.setLogin("user");
        imsDto.setEmail("user@example.com");
        imsDto.setFirstName("First");
        imsDto.setLastName("Last");
        imsDto.setLangKey("en");
        imsDto.setRoles(null);
        ObjectMapper objectMapper = new ObjectMapper();
        String responseBody = objectMapper.writeValueAsString(imsDto);
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getAuthenticateUrl()).thenReturn("/auth");
        when(requestManager.getRequest(anyString(), any())).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        ResponseEntity<AuthenticateResponseDto> result = authenticationService.authenticate(dto);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("user", result.getBody().getDisplayName());
        assertNull(result.getBody().getRoles());
    }

    @Test
    void testAuthenticate_nullToken_shouldHandleGracefully() throws CisException {
        AuthenticateRequestDto dto = new AuthenticateRequestDto();
        when(imsConfig.getBaseUrl()).thenReturn("https://ims");
        when(imsConfig.getAuthenticateUrl()).thenReturn("/auth");
        CisException imsException = new CisException(HttpStatus.UNAUTHORIZED, "invalid token");
        when(requestManager.getRequest(anyString(), any())).thenThrow(imsException);
        CisException ex = assertThrows(CisException.class, () -> authenticationService.authenticate(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

}
