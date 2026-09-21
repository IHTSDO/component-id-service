package org.snomed.cis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.exception.CisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ImsRequestManager {

    private final Logger logger = LoggerFactory.getLogger(ImsRequestManager.class);

    private final RestTemplate restTemplate;
    private final String imsBaseUrl;

    public ImsRequestManager(RestTemplate restTemplate,
                             @Value("${ims.urls.base}") String imsBaseUrl) {
        this.restTemplate = restTemplate;
        this.imsBaseUrl = imsBaseUrl;
    }

    private HttpEntity<Void> withAuth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    /**
     * Get roles (groups) of a user.
     * IMS exposes /user/role?username={username}
     */
    public List<String> getUserGroups(String token, String username) throws CisException {
        try {
            String url = imsBaseUrl + "/user/role?username=" + username;
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    withAuth(token),
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to get user groups from IMS", e);
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get user groups from IMS");
        }
    }

    /**
     * Get users of a group.
     * IMS exposes /group/user?groupname=xxx&username=...&maxResults=...&startAt=...
     */
    public List<String> getGroupUsers(String token, String groupName, int maxResults, int startAt) throws CisException {
        try {
            String url = imsBaseUrl + "/group/user?groupname=" + groupName
                    + "&maxResults=" + maxResults
                    + "&startAt=" + startAt;

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    withAuth(token),
                    new ParameterizedTypeReference<>() {}
            );

            List<Map<String, Object>> body = response.getBody();
            if (body == null) {
                return Collections.emptyList();
            }

            List<String> usernames = new ArrayList<>();
            for (Map<String, Object> userMap : body) {
                Object username = userMap.get("username");
                if (username != null) {
                    usernames.add(username.toString());
                }
            }
            return usernames;

        } catch (Exception e) {
            logger.error("Failed to get group users from IMS", e);
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get group users from IMS");
        }
    }
}

