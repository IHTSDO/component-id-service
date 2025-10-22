package org.snomed.cis.util;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
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

}
