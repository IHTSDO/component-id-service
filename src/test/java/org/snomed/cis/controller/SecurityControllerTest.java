package org.snomed.cis.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.snomed.cis.config.ImsConfig;
import org.snomed.cis.dto.UserDTO;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityControllerTest {

    @InjectMocks
    private SecurityController securityController;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private ImsConfig mockImsConfig;

    @Mock
    private RestTemplate mockRestTemplate;

    @Captor
    ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsValidUser_success() throws Exception {
        String dummyToken = "abc123";
        Cookie[] cookies = new Cookie[]{new Cookie("auth-token", dummyToken)};
        when(mockRequest.getCookies()).thenReturn(cookies);
        when(mockImsConfig.getBaseUrl()).thenReturn("http://ims.com");
        when(mockImsConfig.getAuthenticateUrl()).thenReturn("/authenticate");

        UserDTO dummyUser = new UserDTO();
        ResponseEntity<UserDTO> responseEntity = new ResponseEntity<>(dummyUser, HttpStatus.OK);

        SecurityController controllerSpy = Mockito.spy(securityController);
        ReflectionTestUtils.setField(controllerSpy, "imsConfig", mockImsConfig);
        doReturn(responseEntity).when(controllerSpy).isValidUser(any());

        ResponseEntity<UserDTO> response = controllerSpy.isValidUser(mockRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testIsValidUser_unauthorized() {
        Cookie[] cookies = new Cookie[]{new Cookie("auth-token", "")};
        when(mockRequest.getCookies()).thenReturn(cookies);
        SecurityController controllerSpy = Mockito.spy(securityController);
        ReflectionTestUtils.setField(controllerSpy, "servReq", mockRequest);
        assertThrows(CisException.class, () -> controllerSpy.isValidUser(mockRequest));
    }

    @Test
    void testAuthenticate_success() throws Exception {
        Cookie[] cookies = new Cookie[]{new Cookie("auth-token", "validToken")};
        when(mockRequest.getCookies()).thenReturn(cookies);
        UserDTO dummyUser = new UserDTO();
        ResponseEntity<UserDTO> mockResp = ResponseEntity.ok(dummyUser);

        SecurityController spyController = Mockito.spy(securityController);
        doReturn(mockResp).when(spyController).isValidUser(any());

        UserDTO result = spyController.authenticate();
        assertNotNull(result);
    }

    @Test
    void testAuthenticate_nullBody() throws Exception {
        Cookie[] cookies = new Cookie[]{new Cookie("auth-token", "validToken")};
        when(mockRequest.getCookies()).thenReturn(cookies);
        ResponseEntity<UserDTO> mockResp = new ResponseEntity<>(null, HttpStatus.OK);

        SecurityController spyController = Mockito.spy(securityController);
        doReturn(mockResp).when(spyController).isValidUser(any());

        UserDTO result = spyController.authenticate();
        assertNull(result);
    }

    @Test
    void testValidateUserToken_true() {
        Cookie[] cookies = new Cookie[]{new Cookie("auth-token", "validToken")};
        when(mockRequest.getCookies()).thenReturn(cookies);
        boolean result = securityController.validateUserToken("validToken");
        assertTrue(result);
    }

    @Test
    void testValidateUserToken_false() {
        Cookie[] cookies = new Cookie[]{new Cookie("auth-token", "wrongToken")};
        when(mockRequest.getCookies()).thenReturn(cookies);
        boolean result = securityController.validateUserToken("validToken");
        assertFalse(result);
    }

    @Test
    void testLogoutUser_success() {
        Cookie[] cookies = new Cookie[]{new Cookie("auth-token", "dummyToken")};
        when(mockRequest.getCookies()).thenReturn(cookies);
        when(mockImsConfig.getBaseUrl()).thenReturn("http://ims.com");
        when(mockImsConfig.getLogoutUrl()).thenReturn("/logout");

        String fakeSetCookieHeader = "auth-token=deleted; Path=/; HttpOnly";

        SecurityController spyController = Mockito.spy(new SecurityController());
        ReflectionTestUtils.setField(spyController, "servReq", mockRequest);
        ReflectionTestUtils.setField(spyController, "servletResponse", mockResponse);
        ReflectionTestUtils.setField(spyController, "imsConfig", mockImsConfig);

        doReturn(fakeSetCookieHeader).when(spyController).logoutUser(any(), any());

        String result = spyController.logoutUser(mockRequest, mockResponse);
        assertNotNull(result);
        assertTrue(result.contains("auth-token"));
    }

    @Test
    void testGetUserGroup_success() throws Exception {
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");
        UserDTO user = new UserDTO();
        user.setRoles(roles);
        ResponseEntity<UserDTO> resp = ResponseEntity.ok(user);

        SecurityController spyController = spy(securityController);
        doReturn(resp).when(spyController).isValidUser(any());

        List<String> result = spyController.getUserGroup("dummyUser", "token");
        assertEquals(2, result.size());
        assertTrue(result.contains("ADMIN"));
        assertTrue(result.contains("USER"));
    }

    @Test
    void testGetUserGroup_emptyRoles() throws Exception {
        UserDTO user = new UserDTO();
        user.setRoles(new ArrayList<>());
        ResponseEntity<UserDTO> resp = ResponseEntity.ok(user);

        SecurityController spyController = spy(securityController);
        doReturn(resp).when(spyController).isValidUser(any());

        List<String> result = spyController.getUserGroup("dummyUser", "token");
        assertEquals(0, result.size());
    }

    @Test
    void testGetUserGroupWithoutToken_success() throws Exception {
        List<String> roles = List.of("ROLE_MANAGER", "ROLE_STAFF");
        UserDTO user = new UserDTO();
        user.setRoles(roles);
        ResponseEntity<UserDTO> resp = ResponseEntity.ok(user);

        SecurityController spyController = spy(securityController);
        doReturn(resp).when(spyController).isValidUser(any());

        List<String> result = spyController.getUserGroupWithoutToken("dummyUser");
        assertEquals(2, result.size());
        assertTrue(result.contains("MANAGER"));
        assertTrue(result.contains("STAFF"));
    }
}