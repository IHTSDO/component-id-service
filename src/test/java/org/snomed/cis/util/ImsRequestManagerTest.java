package org.snomed.cis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> body = List.of(
                Map.of("username", "user1"),
                Map.of("username", "user2")
        );

        ResponseEntity<List<Map<String, Object>>> responseEntity = new ResponseEntity<>(body, HttpStatus.OK);
        when(restTemplate.exchange(
                eq(imsBaseUrl + "/group/user?groupname=groupA&maxResults=100&startAt=0"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any())
        ).thenReturn(responseEntity);

        List<String> users = imsRequestManager.getGroupUsers("dummy-token", "groupA", 100, 0);
        assertEquals(2, users.size());
        assertTrue(users.contains("user1"));
        assertTrue(users.contains("user2"));
    }

}

