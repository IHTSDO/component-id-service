package org.snomed.cis.service;


import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.util.ImsRequestManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorizationService {



    private ImsRequestManager imsRequestManager;

    public AuthorizationService(ImsRequestManager imsRequestManager) {
        this.imsRequestManager = imsRequestManager;
    }

    public List<String> getUsers(String token, String searchString) throws CisException {
        return imsRequestManager.getUsers(token, searchString);
    }

    public List<String> getUserGroups(String token, String username) throws CisException {
        return imsRequestManager.getUserGroups(token, username);
    }

    public void removeMember(String token, String username, String groupName) throws CisException {
        imsRequestManager.removeMember(token, username, groupName);
    }

    public void addMember(String token, String username, String groupName) throws CisException {
        imsRequestManager.addMember(token, username, groupName);
    }

    public List<String> getGroupUsers(String token, String groupName) throws CisException {
        // Temporary default: first 100 results starting at 0
        return imsRequestManager.getGroupUsers(token, groupName, 100, 0);
    }


    public List<String> getGroups(String token) throws CisException {
        return imsRequestManager.getGroups(token);
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
