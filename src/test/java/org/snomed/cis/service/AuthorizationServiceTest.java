package org.snomed.cis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.util.ImsRequestManager;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private ImsRequestManager imsRequestManager;


    @Test
    void testGetUserGroups_shouldReturnGroups() throws Exception {
        List<String> expected = List.of("group1", "group2");
        when(imsRequestManager.getUserGroups("dummy-token","user")).thenReturn(expected);
        List<String> result = authorizationService.getUserGroups("dummy-token","user");
        assertEquals(expected, result);
        verify(imsRequestManager).getUserGroups("dummy-token","user");
    }



    @Test
    void testGetUserGroups_shouldThrowCisException() throws Exception {
        when(imsRequestManager.getUserGroups("dummy-token","user")).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid user"));
        CisException ex = assertThrows(CisException.class, () -> authorizationService.getUserGroups("dummy-token","user"));
        assertEquals("Invalid user", ex.getErrorMessage());
    }


    @Test
    void testGetGroupUsers_shouldReturnEmptyList() throws Exception {
        when(imsRequestManager.getGroupUsers(anyString(), eq("group"), eq(100), eq(0))).thenReturn(List.of());
        List<String> result = authorizationService.getGroupUsers("dummy-token","group");
        assertTrue(result.isEmpty());
    }

}
