package org.snomed.cis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.GetStatsResponseDto;
import org.snomed.cis.dto.QueryCountByNamespaceDto;
import org.snomed.cis.pojo.Config;
import org.snomed.cis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private final Logger logger = LoggerFactory.getLogger(StatsService.class);
    @Autowired
    private Config config;

    @Autowired
    private SchemeIdBaseRepository schemeIdBaseRepository;

    @Autowired
    private SctidRepository sctidRepository;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Autowired
    private PermissionsNamespaceRepository permissionsNamespaceRepository;

    @Autowired
    NamespaceRepository namespaceRepository;

    public GetStatsResponseDto getStats(String userName, AuthenticateResponseDto authenticateResponseDto) {
        logger.debug("Request Received : userName-{} :: AuthenticateResponseDto - {} ", userName, authenticateResponseDto);
        GetStatsResponseDto getStatsResponseDto = new GetStatsResponseDto();

        List<String> users = new LinkedList<>();
        List<String> securityAdmins = config.getStats().getSecurityAdmins();
        List<String> securityUsers = config.getStats().getSecurityUsers();
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
            long schemeCount = schemeIdBaseRepository.count();
            getStatsResponseDto.setSchemes(schemeCount);
            List<QueryCountByNamespaceDto> queryCountByNamespaceDtosWithNull = sctidRepository.getCountByNamespace();
            List<QueryCountByNamespaceDto> queryCountByNamespaceDtos = queryCountByNamespaceDtosWithNull.stream().filter(d -> d.getNamespace() != null).collect(Collectors.toList());

            //Long namespaceCount = (long) queryCountByNamespaceDtos.size();
            long namespaceCount = namespaceRepository.count();

            for (QueryCountByNamespaceDto result : queryCountByNamespaceDtos) {
                if (result.getCount() > 0) {
                    namespacesMap.put(result.getNamespace(), result.getCount());
                }
            }
            namespacesMap.put("total", namespaceCount);
            getStatsResponseDto.setNamespaces(namespacesMap);
        } else {
            List<String> otherGroups = new LinkedList<>();
            List<String> namespacesFromGroup = new LinkedList<>();
            List<String> userGroups = authorizationService.getUserGroups(authenticateResponseDto);
            if (userGroups.size() > 0) {
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
            List<PermissionsNamespace> permissionsNamespaces = permissionsNamespaceRepository.findByUsernameIn(otherGroups);
            List<String> namespaceFromPermissionNamespaces = permissionsNamespaces.stream().map(p -> String.valueOf(p.getNamespace())).collect(Collectors.toList());
            List<String> commonNamespaces = new LinkedList<>(namespacesFromGroup);
            commonNamespaces.retainAll(namespaceFromPermissionNamespaces);

            Long totalCount = (long) commonNamespaces.size();
            List<QueryCountByNamespaceDto> queryCountByNamespaceDtosWithNull = sctidRepository.getCountByNamespace(commonNamespaces);
            List<QueryCountByNamespaceDto> queryCountByNamespaceDtos = queryCountByNamespaceDtosWithNull.stream().filter(d -> d.getNamespace() != null).collect(Collectors.toList());

            for (QueryCountByNamespaceDto result : queryCountByNamespaceDtos) {
                if (result.getCount() > 0) {
                    namespacesMap.put(result.getNamespace(), result.getCount());
                }
            }
            namespacesMap.put("total", totalCount);
            getStatsResponseDto.setNamespaces(namespacesMap);
        }
        logger.info("getStats()- Response: {}", getStatsResponseDto);
        return getStatsResponseDto;
    }
}
