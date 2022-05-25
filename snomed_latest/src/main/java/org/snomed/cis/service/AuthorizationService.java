package org.snomed.cis.service;

import org.snomed.cis.controller.dto.AuthenticateResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorizationService {

    public List<String> getUserGroups(AuthenticateResponseDto authenticateResponseDto) {
        return authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
    }

    public List<String> getGroupUsers(AuthenticateResponseDto authenticateResponseDto) {
        return List.of(authenticateResponseDto.getName());
    }

    public List<String> getGroups(AuthenticateResponseDto authenticateResponseDto) {
        return authenticateResponseDto.getRoles().stream().map(s -> s.split("_")[1]).collect(Collectors.toList());
    }

}
