package com.snomed.api.service;

import com.snomed.api.controller.SecurityController;
import com.snomed.api.controller.dto.UserDTO;
import com.snomed.api.exception.APIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthenticateToken {
    @Autowired
    private SecurityController securityController;

    public boolean authenticateToken(String userToken) throws APIException {
        return this.securityController.validateUserToken(userToken);
    }

    public UserDTO getAuthenticatedUser() throws APIException {
        return this.securityController.authenticate();
    }
     public List<String> getGroupsList() throws APIException {
         UserDTO user = this.getAuthenticatedUser();
         List<String> groups = new ArrayList<>();
         for (String roleGroup:
                 user.getRoles()) {
             groups.add(roleGroup.split("_")[1]);
         }
         return groups;
     }
}
