package org.snomed.cis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.dto.UserDTO;
import org.snomed.cis.exception.CisException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticateTokenTest {

    @Mock
    private SecurityController securityController;

    @InjectMocks
    private AuthenticateToken authenticateToken;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateToken_validToken_shouldReturnTrue() throws CisException {
        String token = "valid-token";
        when(securityController.validateUserToken(token)).thenReturn(true);
        assertTrue(authenticateToken.authenticateToken(token));
        verify(securityController).validateUserToken(token);
    }

    @Test
    void testAuthenticateToken_invalidToken_shouldReturnFalse() throws CisException {
        String token = "invalid-token";
        when(securityController.validateUserToken(token)).thenReturn(false);
        assertFalse(authenticateToken.authenticateToken(token));
    }

    @Test
    void testGetAuthenticatedUser_shouldReturnUser() throws CisException {
        UserDTO user = new UserDTO();
        user.setFirstName("Test");
        when(securityController.authenticate()).thenReturn(user);
        UserDTO result = authenticateToken.getAuthenticatedUser();
        assertNotNull(result);
        assertEquals("Test", result.getFirstName());
    }

    @Test
    void testGetGroupsList_shouldReturnGroups() throws CisException {
        UserDTO user = new UserDTO();
        user.setRoles(Arrays.asList("ROLE_ADMIN", "ROLE_TECH"));
        when(securityController.authenticate()).thenReturn(user);
        List<String> groups = authenticateToken.getGroupsList();
        assertEquals(2, groups.size());
        assertEquals("ADMIN", groups.get(0));
        assertEquals("TECH", groups.get(1));
    }

    @Test
    void testGetGroupsList_invalidRoleFormat_shouldThrowException() throws CisException {
        UserDTO user = new UserDTO();
        user.setRoles(List.of("BADFORMAT"));
        when(securityController.authenticate()).thenReturn(user);
        Exception ex = assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            authenticateToken.getGroupsList();
        });
        assertNotNull(ex);
    }
}
