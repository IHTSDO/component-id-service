package org.snomed.cis.util;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.snomed.cis.exception.CisException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImsRequestManagerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ImsRequestManager imsRequestManager;

    private final String imsBaseUrl = "http://fake-ims";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        imsRequestManager = new ImsRequestManager(restTemplate, imsBaseUrl);
    }

    @Test
    void getUsers_withSearchString_shouldReturnUsers() throws Exception {
        List<String> mockUsers = List.of("user1", "user2");
        ResponseEntity<List<String>> responseEntity = new ResponseEntity<>(mockUsers, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(imsBaseUrl + "/users?search=test"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<String>>>any())
        ).thenReturn(responseEntity);

        List<String> users = imsRequestManager.getUsers("dummy-token", "test");
        assertEquals(2, users.size());
        assertTrue(users.contains("user1"));
    }

    @Test
    void getUsers_withNullSearch_shouldCallWithoutQuery() throws Exception {
        List<String> mockUsers = List.of("user1");
        ResponseEntity<List<String>> responseEntity = new ResponseEntity<>(mockUsers, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(imsBaseUrl + "/users"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<String>>>any())
        ).thenReturn(responseEntity);

        List<String> users = imsRequestManager.getUsers("dummy-token", null);
        assertEquals(1, users.size());
        assertEquals("user1", users.get(0));
    }

    @Test
    void getGroups_shouldThrowNotImplemented() {
        CisException ex = assertThrows(CisException.class, () ->
                imsRequestManager.getGroups("dummy-token")
        );
        assertEquals(HttpStatus.NOT_IMPLEMENTED, ex.getStatus());
        assertEquals("Get groups not supported by IMS API", ex.getMessage());
    }


    @Test
    void getUserGroups_shouldReturnGroups() throws Exception {
        List<String> mockGroups = List.of("group1", "group2");
        ResponseEntity<List<String>> responseEntity = new ResponseEntity<>(mockGroups, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(imsBaseUrl + "/user/role?username=john"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<String>>>any())
        ).thenReturn(responseEntity);

        List<String> groups = imsRequestManager.getUserGroups("dummy-token", "john");
        assertEquals(2, groups.size());
        assertTrue(groups.contains("group1"));
    }

    @Test
    void getGroupUsers_shouldReturnUsernames() throws Exception {
        // Mock JsonNode array with two user nodes
        JsonNode user1 = mock(JsonNode.class);
        JsonNode user2 = mock(JsonNode.class);
        when(user1.has("username")).thenReturn(true);
        when(user1.get("username")).thenReturn(mock(JsonNode.class));
        when(user1.get("username").asText()).thenReturn("user1");
        when(user2.has("username")).thenReturn(true);
        when(user2.get("username")).thenReturn(mock(JsonNode.class));
        when(user2.get("username").asText()).thenReturn("user2");

        JsonNode body = mock(JsonNode.class);
        when(body.isArray()).thenReturn(true);
        when(body.iterator()).thenReturn(List.of(user1, user2).iterator());

        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(body, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(imsBaseUrl + "/group/user?groupname=groupA&maxResults=100&startAt=0"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(JsonNode.class))
        ).thenReturn(responseEntity);

        List<String> users = imsRequestManager.getGroupUsers("dummy-token", "groupA", 100, 0);
        assertEquals(2, users.size());
        assertTrue(users.contains("user1"));
        assertTrue(users.contains("user2"));
    }

    @Test
    void addMember_shouldThrowNotImplemented() {
        CisException ex = assertThrows(CisException.class, () ->
                imsRequestManager.addMember("token", "user", "group")
        );
        assertEquals(HttpStatus.NOT_IMPLEMENTED, ex.getStatus());
    }

    @Test
    void removeMember_shouldThrowNotImplemented() {
        CisException ex = assertThrows(CisException.class, () ->
                imsRequestManager.removeMember("token", "user", "group")
        );
        assertEquals(HttpStatus.NOT_IMPLEMENTED, ex.getStatus());
    }
}
