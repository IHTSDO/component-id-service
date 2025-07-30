package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.util.CrowdRequestManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorizationService {
    private final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    @Autowired
    CrowdRequestManager crowdRequestManager;

    public List<String> getUsers(String searchString) throws CisException {
        return crowdRequestManager.getUsers(searchString);
    }

    public List<String> getUserGroups(String username) throws CisException {
        return crowdRequestManager.getUserGroups(username);
    }

    public void removeMember(String username, String groupName) throws CisException {
        crowdRequestManager.removeMember(username, groupName);
    }

    public void addMember(String username, String groupName) throws CisException {
        crowdRequestManager.addMember(username, groupName);
    }

    public List<String> getGroupUsers(String groupName) throws CisException {
        return crowdRequestManager.getGroupUsers(groupName);
    }

    public List<String> getGroups() throws CisException {
        return crowdRequestManager.getGroups();
    }

    private boolean isAdmin(AuthenticateResponseDto authenticateResponseDto) {
        List<String> roles = authenticateResponseDto.getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role.toLowerCase().contains("component-identifier-service-admin"));
    }

    public void validateAdmin(Token token) throws CisException {
        if (!this.isAdmin(token.getAuthenticateResponseDto())) {
            throw new CisException(HttpStatus.FORBIDDEN, "Access denied: Admin access required");
        }
    }

    public void validateSelfOrAdmin(Token token, String username) throws CisException {
        if (!this.isAdmin(token.getAuthenticateResponseDto())
                && !username.equalsIgnoreCase(token.getUserName())) {
            throw new CisException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }
}
