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
    void testGetUsers_shouldReturnList() throws Exception {
        List<String> expected = List.of("user1", "user2");
        when(imsRequestManager.getUsers(anyString(), eq("search"))).thenReturn(expected);
        List<String> result = authorizationService.getUsers("dummy-token","search");
        assertEquals(expected, result);
        verify(imsRequestManager).getUsers("dummy-token","search");
    }

    @Test
    void testGetUserGroups_shouldReturnGroups() throws Exception {
        List<String> expected = List.of("group1", "group2");
        when(imsRequestManager.getUserGroups("dummy-token","user")).thenReturn(expected);
        List<String> result = authorizationService.getUserGroups("dummy-token","user");
        assertEquals(expected, result);
        verify(imsRequestManager).getUserGroups("dummy-token","user");
    }

    @Test
    void testAddMember_shouldInvokeImsRequestManager() throws Exception {
        authorizationService.addMember("dummy-token","user", "group");
        verify(imsRequestManager).addMember("dummy-token","user", "group");
    }

    @Test
    void testRemoveMember_shouldInvokeImsRequestManager() throws Exception {
        authorizationService.removeMember("dummy-token","user", "group");
        verify(imsRequestManager).removeMember("dummy-token","user", "group");
    }

    @Test
    void testGetGroupUsers_shouldReturnList() throws Exception {
        List<String> expected = List.of("user1", "user2");
        when(imsRequestManager.getGroupUsers("dummy-token","group",100,0)).thenReturn(expected);
        List<String> result = authorizationService.getGroupUsers("dummy-token","group");
        assertEquals(expected, result);
        verify(imsRequestManager).getGroupUsers("dummy-token","group",100,0);
    }

    @Test
    void testGetGroups_shouldReturnList() throws Exception {
        List<String> expected = List.of("group1", "group2");
        when(imsRequestManager.getGroups("dummy-token")).thenReturn(expected);
        List<String> result = authorizationService.getGroups("dummy-token");
        assertEquals(expected, result);
        verify(imsRequestManager).getGroups("dummy-token");
    }

    @Test
    void testGetUsers_shouldThrowCisException() throws Exception {
        when(imsRequestManager.getUsers("dummy-token","search")).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Error"));
        CisException ex = assertThrows(CisException.class, () -> authorizationService.getUsers("dummy-token","search"));
        assertEquals("Error", ex.getErrorMessage());
        verify(imsRequestManager).getUsers("dummy-token","search");
    }

    @Test
    void testGetUserGroups_shouldThrowCisException() throws Exception {
        when(imsRequestManager.getUserGroups("dummy-token","user")).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid user"));
        CisException ex = assertThrows(CisException.class, () -> authorizationService.getUserGroups("dummy-token","user"));
        assertEquals("Invalid user", ex.getErrorMessage());
    }

    @Test
    void testAddMember_shouldThrowCisException() throws Exception {
        doThrow(new CisException(HttpStatus.CONFLICT, "Already member")).when(imsRequestManager).addMember("dummy-token","user", "group");
        CisException ex = assertThrows(CisException.class, () -> authorizationService.addMember("dummy-token","user", "group"));
        assertEquals("Already member", ex.getErrorMessage());
    }

    @Test
    void testRemoveMember_shouldThrowCisException() throws Exception {
        doThrow(new CisException(HttpStatus.NOT_FOUND, "User not in group")).when(imsRequestManager).removeMember("dummy-token","user", "group");
        CisException ex = assertThrows(CisException.class, () -> authorizationService.removeMember("dummy-token","user", "group"));
        assertEquals("User not in group", ex.getErrorMessage());
    }

    @Test
    void testGetGroupUsers_shouldReturnEmptyList() throws Exception {
        when(imsRequestManager.getGroupUsers(anyString(), eq("group"), eq(100), eq(0))).thenReturn(List.of());
        List<String> result = authorizationService.getGroupUsers("dummy-token","group");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetGroups_nullFromManager_shouldReturnNull() throws Exception {
        when(imsRequestManager.getGroups("dummy-token")).thenReturn(null);
        List<String> result = authorizationService.getGroups("dummy-token");
        assertNull(result);
    }

}
