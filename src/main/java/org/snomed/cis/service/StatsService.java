package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.GetStatsResponseDto;
import org.snomed.cis.dto.QueryCountByNamespaceDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.*;
import org.snomed.cis.util.ImsRequestManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Service
public class StatsService {
    private final Logger logger = LoggerFactory.getLogger(StatsService.class);


    public StatsService(ImsRequestManager ims) {
        this.ims = ims;
    }

    private ImsRequestManager ims;
    @Autowired
    SchemeIdBaseRepository schemeIdBaseRepository;

    @Autowired
    SctidRepository sctidRepository;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    PermissionsSchemeRepository permissionsSchemeRepository;

    @Autowired
    PermissionsNamespaceRepository permissionsNamespaceRepository;

    @Autowired
    NamespaceRepository namespaceRepository;

    public GetStatsResponseDto getStats(String token, String userName, AuthenticateResponseDto authenticateResponseDto) throws CisException {
        logger.info("Request Received : getStats() for username - {} ", authenticateResponseDto.getName());
        GetStatsResponseDto getStatsResponseDto = new GetStatsResponseDto();

        List<String> users = new LinkedList<>();
        List<String> securityAdmins = ims.getGroupUsers(token,"component-identifier-service-admin",1000,0);
        List<String> securityUsers = ims.getGroupUsers(token,"component-identifier-service-user",1000,0);
        boolean adminU = false;

        for (String admin : securityAdmins) {
            if (admin.equalsIgnoreCase(userName))
                adminU = true;
            if (!users.contains(admin))
                users.add(admin);
        }
        for (String user : securityUsers) {
            if (!users.contains(user))
                users.add(user);
        }

        getStatsResponseDto.setUsers((long) users.size());
        HashMap<String, Long> namespacesMap = new HashMap<>();

        if (adminU) {
            processAdminStats(getStatsResponseDto, namespacesMap);
        } else {
            processNonAdminStats(token, userName, getStatsResponseDto, namespacesMap);
        }

        logger.debug("getStats()- Response: {}", getStatsResponseDto);
        return getStatsResponseDto;
    }

    // --- helper method for admin ---
    private void processAdminStats(GetStatsResponseDto getStatsResponseDto, HashMap<String, Long> namespacesMap) {
        long schemeCount = schemeIdBaseRepository.count();
        getStatsResponseDto.setSchemes(schemeCount);

        List<QueryCountByNamespaceDto> queryCountByNamespaceDtos = sctidRepository.getCountByNamespace()
                .stream().filter(d -> d.getNamespace() != null).toList();

        long namespaceCount = namespaceRepository.count();

        for (QueryCountByNamespaceDto result : queryCountByNamespaceDtos) {
            if (result.getCount() > 0) {
                namespacesMap.put(result.getNamespace(), result.getCount());
            }
        }
        namespacesMap.put("total", namespaceCount);
        getStatsResponseDto.setNamespaces(namespacesMap);
    }

    // --- helper method for non-admin ---
    private void processNonAdminStats(String token, String userName, GetStatsResponseDto getStatsResponseDto, HashMap<String, Long> namespacesMap) throws CisException {
        List<String> otherGroups = new LinkedList<>();
        List<String> namespacesFromGroup = new LinkedList<>();
        List<String> userGroups = authorizationService.getUserGroups(token,userName);

        if (!userGroups.isEmpty()) {
            for (String group : userGroups) {
                String groupName = group.substring(0, group.indexOf("-"));
                if ("namespace".equals(groupName))
                    namespacesFromGroup.add(group.substring(group.indexOf("-") + 1));
                else
                    otherGroups.add(group);
            }
        }

        Long schemeCount = permissionsSchemeRepository.countByUsernameIn(otherGroups);
        getStatsResponseDto.setSchemes(schemeCount);

        List<String> namespaceFromPermissionNamespaces = permissionsNamespaceRepository.findByUsernameIn(otherGroups)
                .stream().map(p -> String.valueOf(p.getNamespace())).toList();

        List<String> commonNamespaces = new LinkedList<>(namespacesFromGroup);
        commonNamespaces.retainAll(namespaceFromPermissionNamespaces);

        Long totalCount = (long) commonNamespaces.size();

        List<QueryCountByNamespaceDto> queryCountByNamespaceDtos = sctidRepository.getCountByNamespace(commonNamespaces)
                .stream().filter(d -> d.getNamespace() != null).toList();

        for (QueryCountByNamespaceDto result : queryCountByNamespaceDtos) {
            if (result.getCount() > 0) {
                namespacesMap.put(result.getNamespace(), result.getCount());
            }
        }

        namespacesMap.put("total", totalCount);
        getStatsResponseDto.setNamespaces(namespacesMap);
    }

}
