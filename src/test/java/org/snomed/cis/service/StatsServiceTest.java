package org.snomed.cis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.GetStatsResponseDto;
import org.snomed.cis.dto.QueryCountByNamespaceDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.*;
import org.snomed.cis.util.ImsRequestManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    private StatsService statsService;

    @Mock private ImsRequestManager imsRequestManager;
    @Mock private SchemeIdBaseRepository schemeIdBaseRepository;
    @Mock private SctidRepository sctidRepository;
    @Mock private AuthorizationService authorizationService;
    @Mock private PermissionsSchemeRepository permissionsSchemeRepository;
    @Mock private PermissionsNamespaceRepository permissionsNamespaceRepository;
    @Mock private NamespaceRepository namespaceRepository;

    private final String ADMIN_USERNAME = "adminUser";
    private final String NORMAL_USERNAME = "normalUser";

    private AuthenticateResponseDto authDto;

    @BeforeEach
    void setUp() {
        // build service manually
        statsService = new StatsService(imsRequestManager);
        statsService.schemeIdBaseRepository = schemeIdBaseRepository;
        statsService.sctidRepository = sctidRepository;
        statsService.authorizationService = authorizationService;
        statsService.permissionsSchemeRepository = permissionsSchemeRepository;
        statsService.permissionsNamespaceRepository = permissionsNamespaceRepository;
        statsService.namespaceRepository = namespaceRepository;

        // common auth dto
        authDto = AuthenticateResponseDto.builder()
                .name("adminUser")
                .firstName("Admin")
                .lastName("User")
                .displayName("Admin User")
                .email("admin@example.com")
                .langKey("en")
                .roles(List.of("admin-role"))
                .build();
    }

    @Test
    void testGetStats_AsAdminUser() throws CisException {
        List<String> admins = List.of(ADMIN_USERNAME);
        List<String> users = List.of(ADMIN_USERNAME, "user2");

        QueryCountByNamespaceDto dto1 = Mockito.mock(QueryCountByNamespaceDto.class);
        Mockito.when(dto1.getNamespace()).thenReturn("1234");
        Mockito.when(dto1.getCount()).thenReturn(10L);

        Mockito.when(imsRequestManager.getGroupUsers("dummy-token","component-identifier-service-admin",-1,0)).thenReturn(admins);
        Mockito.when(imsRequestManager.getGroupUsers("dummy-token","component-identifier-service-user",-1,0)).thenReturn(users);
        Mockito.when(schemeIdBaseRepository.count()).thenReturn(5L);
        Mockito.when(sctidRepository.getCountByNamespace()).thenReturn(List.of(dto1));
        Mockito.when(namespaceRepository.count()).thenReturn(1L);

        GetStatsResponseDto result = statsService.getStats("dummy-token", ADMIN_USERNAME, authDto);

        assertEquals(2L, result.getUsers());
        assertEquals(5L, result.getSchemes());
        assertEquals(10L, result.getNamespaces().get("1234"));
        assertEquals(1L, result.getNamespaces().get("total"));
    }

    @Test
    void testGetStats_AsNormalUser() throws CisException {
        List<String> admins = List.of("otherAdmin");
        List<String> users = List.of("user1", "user2");
        List<String> userGroups = List.of("namespace-111", "group-role");

        PermissionsNamespace pns = Mockito.mock(PermissionsNamespace.class);
        Mockito.when(pns.getNamespace()).thenReturn(111);
        List<PermissionsNamespace> permissionsNamespaces = List.of(pns);

        QueryCountByNamespaceDto dto = Mockito.mock(QueryCountByNamespaceDto.class);
        Mockito.when(dto.getNamespace()).thenReturn("111");
        Mockito.when(dto.getCount()).thenReturn(20L);

        Mockito.when(imsRequestManager.getGroupUsers("dummy-token","component-identifier-service-admin",-1,0)).thenReturn(admins);
        Mockito.when(imsRequestManager.getGroupUsers("dummy-token","component-identifier-service-user",-1,0)).thenReturn(users);
        Mockito.when(authorizationService.getUserGroups("dummy-token", NORMAL_USERNAME)).thenReturn(userGroups);
        Mockito.when(permissionsSchemeRepository.countByUsernameIn(List.of("group-role"))).thenReturn(3L);
        Mockito.when(permissionsNamespaceRepository.findByUsernameIn(List.of("group-role"))).thenReturn(permissionsNamespaces);
        Mockito.when(sctidRepository.getCountByNamespace(List.of("111"))).thenReturn(List.of(dto));

        GetStatsResponseDto result = statsService.getStats("dummy-token", NORMAL_USERNAME, authDto);

        assertEquals(3L, result.getUsers());
        assertEquals(3L, result.getSchemes());
        assertEquals(20L, result.getNamespaces().get("111"));
        assertEquals(1L, result.getNamespaces().get("total"));
    }

    @Test
    void testGetStats_NoGroupsNoPermissions() throws CisException {
        Mockito.when(imsRequestManager.getGroupUsers("dummy-token","component-identifier-service-admin",-1,0)).thenReturn(List.of());
        Mockito.when(imsRequestManager.getGroupUsers("dummy-token","component-identifier-service-user",-1,0)).thenReturn(List.of());
        Mockito.when(authorizationService.getUserGroups("dummy-token", NORMAL_USERNAME)).thenReturn(List.of());

        Mockito.when(permissionsSchemeRepository.countByUsernameIn(List.of())).thenReturn(0L);
        Mockito.when(permissionsNamespaceRepository.findByUsernameIn(List.of())).thenReturn(List.of());

        GetStatsResponseDto result = statsService.getStats("dummy-token", NORMAL_USERNAME, authDto);

        assertEquals(0L, result.getSchemes());
        assertEquals(0L, result.getNamespaces().get("total"));
    }
}
