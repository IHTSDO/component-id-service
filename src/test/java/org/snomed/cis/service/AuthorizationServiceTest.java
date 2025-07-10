package org.snomed.cis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.util.CrowdRequestManager;
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
    private CrowdRequestManager crowdRequestManager;

    @Test
    void testGetUsers_shouldReturnList() throws Exception {
        List<String> expected = List.of("user1", "user2");
        when(crowdRequestManager.getUsers("search")).thenReturn(expected);
        List<String> result = authorizationService.getUsers("search");
        assertEquals(expected, result);
        verify(crowdRequestManager).getUsers("search");
    }

    @Test
    void testGetUserGroups_shouldReturnGroups() throws Exception {
        List<String> expected = List.of("group1", "group2");
        when(crowdRequestManager.getUserGroups("user")).thenReturn(expected);
        List<String> result = authorizationService.getUserGroups("user");
        assertEquals(expected, result);
        verify(crowdRequestManager).getUserGroups("user");
    }

    @Test
    void testAddMember_shouldInvokeCrowdRequestManager() throws Exception {
        authorizationService.addMember("user", "group");
        verify(crowdRequestManager).addMember("user", "group");
    }

    @Test
    void testRemoveMember_shouldInvokeCrowdRequestManager() throws Exception {
        authorizationService.removeMember("user", "group");
        verify(crowdRequestManager).removeMember("user", "group");
    }

    @Test
    void testGetGroupUsers_shouldReturnList() throws Exception {
        List<String> expected = List.of("user1", "user2");
        when(crowdRequestManager.getGroupUsers("group")).thenReturn(expected);
        List<String> result = authorizationService.getGroupUsers("group");
        assertEquals(expected, result);
        verify(crowdRequestManager).getGroupUsers("group");
    }

    @Test
    void testGetGroups_shouldReturnList() throws Exception {
        List<String> expected = List.of("group1", "group2");
        when(crowdRequestManager.getGroups()).thenReturn(expected);
        List<String> result = authorizationService.getGroups();
        assertEquals(expected, result);
        verify(crowdRequestManager).getGroups();
    }

    @Test
    void testGetUsers_shouldThrowCisException() throws Exception {
        when(crowdRequestManager.getUsers("search")).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Error"));
        CisException ex = assertThrows(CisException.class, () -> authorizationService.getUsers("search"));
        assertEquals("Error", ex.getErrorMessage());
        verify(crowdRequestManager).getUsers("search");
    }

    @Test
    void testGetUserGroups_shouldThrowCisException() throws Exception {
        when(crowdRequestManager.getUserGroups("user")).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid user"));
        CisException ex = assertThrows(CisException.class, () -> authorizationService.getUserGroups("user"));
        assertEquals("Invalid user", ex.getErrorMessage());
    }

    @Test
    void testAddMember_shouldThrowCisException() throws Exception {
        doThrow(new CisException(HttpStatus.CONFLICT, "Already member")).when(crowdRequestManager).addMember("user", "group");
        CisException ex = assertThrows(CisException.class, () -> authorizationService.addMember("user", "group"));
        assertEquals("Already member", ex.getErrorMessage());
    }

    @Test
    void testRemoveMember_shouldThrowCisException() throws Exception {
        doThrow(new CisException(HttpStatus.NOT_FOUND, "User not in group")).when(crowdRequestManager).removeMember("user", "group");
        CisException ex = assertThrows(CisException.class, () -> authorizationService.removeMember("user", "group"));
        assertEquals("User not in group", ex.getErrorMessage());
    }

    @Test
    void testGetGroupUsers_shouldReturnEmptyList() throws Exception {
        when(crowdRequestManager.getGroupUsers("group")).thenReturn(List.of());
        List<String> result = authorizationService.getGroupUsers("group");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetGroups_nullFromManager_shouldReturnNull() throws Exception {
        when(crowdRequestManager.getGroups()).thenReturn(null);
        List<String> result = authorizationService.getGroups();
        assertNull(result);
    }

}
