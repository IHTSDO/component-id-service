package org.snomed.cis.util;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.client.CrowdClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrowdRequestManagerTest {

    private CrowdRequestManager crowdRequestManager;
    private CrowdClient mockCrowdClient;

    @BeforeEach
    void setup() throws Exception {
        var constructor = CrowdRequestManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        crowdRequestManager = constructor.newInstance();
        mockCrowdClient = mock(CrowdClient.class);
        var field = CrowdRequestManager.class.getDeclaredField("crowdClient");
        field.setAccessible(true);
        field.set(crowdRequestManager, mockCrowdClient);
    }


    @Test
    void testGetUsers_success() throws Exception {
        when(mockCrowdClient.searchUserNames(any(), eq(0), anyInt())).thenReturn(List.of("user1", "user2"));
        List<String> result = crowdRequestManager.getUsers("user");
        assertEquals(2, result.size());
        assertTrue(result.contains("user1"));
    }

    @Test
    void testAddMember_userAlreadyExists() throws Exception {
        doThrow(new MembershipAlreadyExistsException("user1", "group1")).when(mockCrowdClient).addUserToGroup("user1", "group1");
        assertDoesNotThrow(() -> crowdRequestManager.addMember("user1", "group1"));
    }

    @Test
    void testAddMember_userNotFound_shouldThrowCisException() {
        assertThrows(CisException.class, () -> {
            doThrow(new UserNotFoundException("invalidUser")).when(mockCrowdClient).addUserToGroup(any(), any());
            crowdRequestManager.addMember("invalidUser", "group");
        });

    }

    @Test
    void testGetGroups_success() throws Exception {
        when(mockCrowdClient.searchGroupNames(any(), eq(0), anyInt())).thenReturn(List.of("admin", "devs"));
        List<String> groups = crowdRequestManager.getGroups();
        assertEquals(2, groups.size());
    }

    @Test
    void testGetGroupUsers_success() throws Exception {
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        when(user1.getName()).thenReturn("user1");
        when(user2.getName()).thenReturn("user2");
        when(mockCrowdClient.getUsersOfGroup("testGroup", 0, Integer.MAX_VALUE)).thenReturn(List.of(user1, user2));
        List<String> users = crowdRequestManager.getGroupUsers("testGroup");
        assertEquals(List.of("user1", "user2"), users);
    }

    @Test
    void testRemoveMember_groupNotFound_shouldThrowCisException() {
        assertThrows(CisException.class, () -> {
            doThrow(new GroupNotFoundException("nonexistentGroup")).when(mockCrowdClient).removeUserFromGroup(any(), any());
            crowdRequestManager.removeMember("user", "nonexistentGroup");
        });

    }

    @Test
    void testGetUserGroups_success() throws Exception {
        Group group1 = mock(Group.class);
        Group group2 = mock(Group.class);
        when(group1.getName()).thenReturn("group1");
        when(group2.getName()).thenReturn("group2");
        when(mockCrowdClient.getGroupsForUser("user1", 0, Integer.MAX_VALUE)).thenReturn(List.of(group1, group2));
        List<String> result = crowdRequestManager.getUserGroups("user1");
        assertEquals(List.of("group1", "group2"), result);
    }

    @Test
    void testGetUsers_invalidAuth_shouldThrowCisException() throws Exception {
        when(mockCrowdClient.searchUserNames(any(), anyInt(), anyInt())).thenThrow(new InvalidAuthenticationException("Invalid credentials"));
        CisException ex = assertThrows(CisException.class, () -> crowdRequestManager.getUsers("test"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testGetUsers_withNullSearchString_shouldReturnUserList() throws Exception {
        when(mockCrowdClient.searchUserNames(any(), anyInt(), anyInt())).thenReturn(List.of("user1", "user2"));
        List<String> result = crowdRequestManager.getUsers(null);
        assertEquals(List.of("user1", "user2"), result);
    }

    @Test
    void testGetUsers_applicationPermissionException_shouldThrowCisException() throws Exception {
        when(mockCrowdClient.searchUserNames(any(), anyInt(), anyInt())).thenThrow(new ApplicationPermissionException("Not allowed"));
        assertThrows(CisException.class, () -> crowdRequestManager.getUsers("test"));
    }

    @Test
    void testGetGroups_invalidAuth_shouldThrowCisException() throws Exception {
        when(mockCrowdClient.searchGroupNames(any(), anyInt(), anyInt())).thenThrow(new InvalidAuthenticationException("Invalid credentials"));
        assertThrows(CisException.class, () -> crowdRequestManager.getGroups());
    }

    @Test
    void testGetGroupUsers_validGroup_shouldReturnUserNames() throws Exception {
        List<User> mockUsers = List.of(mockUser("user1"), mockUser("user2"));
        when(mockCrowdClient.getUsersOfGroup("group1", 0, Integer.MAX_VALUE)).thenReturn(mockUsers);
        List<String> result = crowdRequestManager.getGroupUsers("group1");
        assertEquals(List.of("user1", "user2"), result);
    }

    @Test
    void testGetGroupUsers_groupNotFound_shouldThrowCisException() throws Exception {
        when(mockCrowdClient.getUsersOfGroup(any(), anyInt(), anyInt())).thenThrow(new GroupNotFoundException("group1"));
        assertThrows(CisException.class, () -> crowdRequestManager.getGroupUsers("group1"));
    }

    @Test
    void testGetUserGroups_userNotFound_shouldThrowCisException() throws Exception {
        when(mockCrowdClient.getGroupsForUser(eq("invalidUser"), anyInt(), anyInt())).thenThrow(new UserNotFoundException("invalidUser"));
        assertThrows(CisException.class, () -> crowdRequestManager.getUserGroups("invalidUser"));
    }

    @Test
    void testAddMember_membershipAlreadyExists_shouldNotThrowException() throws Exception {
        doThrow(new MembershipAlreadyExistsException("user1", "group1")).when(mockCrowdClient).addUserToGroup("user1", "group1");
        assertDoesNotThrow(() -> crowdRequestManager.addMember("user1", "group1"));
        verify(mockCrowdClient).addUserToGroup("user1", "group1"); // ensures method was called
    }

    @Test
    void testRemoveMember_membershipNotFound_shouldThrowCisException() throws Exception {
        doThrow(new MembershipNotFoundException("user1", "group1")).when(mockCrowdClient).removeUserFromGroup("user1", "group1");
        assertThrows(CisException.class, () -> crowdRequestManager.removeMember("user1", "group1"));
    }

    private User mockUser(String username) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(username);
        return user;
    }

}
