package org.snomed.cis.util;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.restriction.NullRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.service.client.CrowdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.exception.CisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CrowdRequestManager {

    private final Logger logger = LoggerFactory.getLogger(CrowdRequestManager.class);

    @Autowired
    CrowdClient crowdClient;

    private CrowdRequestManager() {
    }

    public List<String> getUsers(String searchString) throws CisException {
        SearchRestriction searchRestriction;
        if (searchString == null)
            searchRestriction = NullRestriction.INSTANCE;
        else
            searchRestriction = Restriction.on(UserTermKeys.USERNAME).startingWith(searchString);
        List<String> users;
        try {
            users = crowdClient.searchUserNames(searchRestriction, 0, Integer.MAX_VALUE);
        } catch (InvalidAuthenticationException e) {
            logger.error("application name/password invalid", e);
            throw new CisException(HttpStatus.NOT_FOUND, "application name/password invalid");
        } catch (ApplicationPermissionException e) {
            logger.error("application not permitted to perform the operation", e);
            throw new CisException(HttpStatus.FORBIDDEN, "application not permitted to perform the operation");
        } catch (OperationFailedException e) {
            logger.error("unknown error from crowd", e);
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error from crowd");
        }
        return users;
    }

    public void addMember(String username, String groupName) throws CisException {
        try {
            crowdClient.addUserToGroup(username, groupName);
        } catch (InvalidAuthenticationException e) {
            logger.error("application name/password invalid");
            throw new CisException(HttpStatus.NOT_FOUND, "application name/password invalid");
        } catch (ApplicationPermissionException e) {
            logger.error("application not permitted to perform the operation");
            throw new CisException(HttpStatus.FORBIDDEN, "application not permitted to perform the operation");
        } catch (UserNotFoundException e) {
            logger.error("user not found");
            throw new CisException(HttpStatus.NOT_FOUND, "user not found");
        } catch (GroupNotFoundException e) {
            logger.error("group not found");
            throw new CisException(HttpStatus.NOT_FOUND, "group not found");
        } catch (MembershipAlreadyExistsException e) {
            logger.error("user already member of group");
        } catch (OperationFailedException e) {
            logger.error("unknown error from crowd");
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error from crowd");
        }
    }

    public List<String> getGroups() throws CisException {
        List<String> groups;
        try {
            SearchRestriction searchRestriction = NullRestriction.INSTANCE;
            groups = crowdClient.searchGroupNames(searchRestriction, 0, Integer.MAX_VALUE);
        } catch (InvalidAuthenticationException e) {
            logger.error("application name/password invalid");
            throw new CisException(HttpStatus.NOT_FOUND, "application name/password invalid");
        } catch (ApplicationPermissionException e) {
            logger.error("application not permitted to perform the operation");
            throw new CisException(HttpStatus.FORBIDDEN, "application not permitted to perform the operation");
        } catch (OperationFailedException e) {
            logger.error("unknown error from crowd");
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error from crowd");
        }
        return groups;
    }

    public List<String> getGroupUsers(String groupName) throws CisException {
        List<User> users;
        try {
            users = crowdClient.getUsersOfGroup(groupName, 0, Integer.MAX_VALUE);
        } catch (InvalidAuthenticationException e) {
            logger.error("Application name/password invalid, for group: {}", groupName);
            throw new CisException(HttpStatus.NOT_FOUND, "application name/password invalid");
        } catch (ApplicationPermissionException e) {
            logger.error("Application not permitted to perform the operation for group name: {}", groupName);
            throw new CisException(HttpStatus.FORBIDDEN, "application not permitted to perform the operation");
        } catch (GroupNotFoundException e) {
            logger.error("Group not found: {}", groupName);
            throw new CisException(HttpStatus.NOT_FOUND, "group not found");
        } catch (OperationFailedException e) {
            logger.error("Unknown error from crowd for group: {}", groupName);
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error from crowd");
        }
        return users.stream().map(User::getName).collect(Collectors.toList());
    }

    public void removeMember(String username, String groupName) throws CisException {
        try {
            crowdClient.removeUserFromGroup(username, groupName);
        } catch (InvalidAuthenticationException e) {
            logger.error("application name/password invalid");
            throw new CisException(HttpStatus.NOT_FOUND, "application name/password invalid");
        } catch (ApplicationPermissionException e) {
            logger.error("application not permitted to perform the operation");
            throw new CisException(HttpStatus.FORBIDDEN, "application not permitted to perform the operation");
        } catch (UserNotFoundException e) {
            logger.error("user not found");
            throw new CisException(HttpStatus.NOT_FOUND, "user not found");
        } catch (GroupNotFoundException e) {
            logger.error("group not found");
            throw new CisException(HttpStatus.NOT_FOUND, "group not found");
        } catch (MembershipNotFoundException e) {
            logger.error("user not part of the group");
            throw new CisException(HttpStatus.NOT_FOUND, "user not part of the group");
        } catch (OperationFailedException e) {
            logger.error("unknown error from crowd");
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error from crowd");
        }
    }

    public List<String> getUserGroups(String username) throws CisException {
        List<Group> groups;
        try {
            groups = crowdClient.getGroupsForUser(username, 0, Integer.MAX_VALUE);
        } catch (InvalidAuthenticationException e) {
            logger.error("application name/password invalid");
            throw new CisException(HttpStatus.NOT_FOUND, "application name/password invalid");
        } catch (ApplicationPermissionException e) {
            logger.error("application not permitted to perform the operation");
            throw new CisException(HttpStatus.FORBIDDEN, "application not permitted to perform the operation");
        } catch (UserNotFoundException e) {
            logger.error("user not found");
            throw new CisException(HttpStatus.NOT_FOUND, "user not found");
        } catch (OperationFailedException e) {
            logger.error("unknown error from crowd");
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "unknown error from crowd");
        }
        return groups.stream().map(Group::getName).collect(Collectors.toList());
    }

}
