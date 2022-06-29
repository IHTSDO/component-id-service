package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.util.CrowdRequestManager;
import org.springframework.beans.factory.annotation.Autowired;
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

}
