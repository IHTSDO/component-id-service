package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.controller.dto.UserDTO;
import org.snomed.cis.exception.CisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthenticateToken {
    private final Logger logger = LoggerFactory.getLogger(AuthenticateToken.class);
    @Autowired
    private SecurityController securityController;

    public boolean authenticateToken(String userToken) throws CisException {
        return this.securityController.validateUserToken(userToken);
    }

    public UserDTO getAuthenticatedUser() throws CisException {
        return this.securityController.authenticate();
    }
     public List<String> getGroupsList() throws CisException {
         UserDTO user = this.getAuthenticatedUser();
         List<String> groups = new ArrayList<>();
         for (String roleGroup:
                 user.getRoles()) {
             groups.add(roleGroup.split("_")[1]);
         }
         return groups;
     }
}
