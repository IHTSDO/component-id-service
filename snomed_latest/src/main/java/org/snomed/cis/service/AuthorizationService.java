package org.snomed.cis.service;

import org.snomed.cis.controller.dto.AuthenticateResponseDto;
import org.snomed.cis.pojo.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorizationService {

    @Autowired
    private Config config;

    public List<String> getUsers() {
        return config.getAuthorization().getUsers();
    }

    public List<String> getUserGroups(AuthenticateResponseDto authenticateResponseDto) {
        return authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
    }

    public void deleteMember(String username, String groupName) {
    }

    public void addMember(String username, String groupName) {
    }

    public List<String> getGroupUsers(String groupName) {
        return config.getAuthorization().getUsers();
    }

    public List<String> getGroups() {
        return config.getAuthorization().getGroups();
    }

}
