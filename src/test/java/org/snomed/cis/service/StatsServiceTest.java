package org.snomed.cis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.GetStatsResponseDto;
import org.snomed.cis.dto.QueryCountByNamespaceDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.*;
import org.snomed.cis.util.CrowdRequestManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @InjectMocks
    private StatsService statsService;

    @Mock
    private CrowdRequestManager crowdRequestManager;
    @Mock private SchemeIdBaseRepository schemeIdBaseRepository;
    @Mock private SctidRepository sctidRepository;
    @Mock private AuthorizationService authorizationService;
    @Mock private PermissionsSchemeRepository permissionsSchemeRepository;
    @Mock private PermissionsNamespaceRepository permissionsNamespaceRepository;
    @Mock private NamespaceRepository namespaceRepository;

    private final String ADMIN_USERNAME = "adminUser";
    private final String NORMAL_USERNAME = "normalUser";
    @Mock
    private AuthenticateResponseDto authDto;

    @BeforeEach
    void setUp() {
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


        // ✅ Admin user stats test
        @Test
        void testGetStats_AsAdminUser() throws CisException {
            List<String> admins = List.of(ADMIN_USERNAME);
            List<String> users = List.of(ADMIN_USERNAME, "user2");

            // Mock namespace count DTO with valid namespace
            QueryCountByNamespaceDto dto1 = Mockito.mock(QueryCountByNamespaceDto.class);
            Mockito.when(dto1.getNamespace()).thenReturn("1234");
            Mockito.when(dto1.getCount()).thenReturn(10L);

            List<QueryCountByNamespaceDto> namespaceCounts = List.of(dto1); // ❌ dto2 removed to avoid unnecessary stubbing

            // Mock repository/service behavior
            Mockito.when(crowdRequestManager.getGroupUsers("component-identifier-service-admin")).thenReturn(admins);
            Mockito.when(crowdRequestManager.getGroupUsers("component-identifier-service-user")).thenReturn(users);
            Mockito.when(schemeIdBaseRepository.count()).thenReturn(5L);
            Mockito.when(sctidRepository.getCountByNamespace()).thenReturn(namespaceCounts);
            Mockito.when(namespaceRepository.count()).thenReturn(1L);

            // Call service
            GetStatsResponseDto result = statsService.getStats(ADMIN_USERNAME, authDto);

            // Validate response
            assertEquals(2L, result.getUsers());
            assertEquals(5L, result.getSchemes());
            assertTrue(result.getNamespaces().containsKey("1234"));
            assertEquals(10L, result.getNamespaces().get("1234"));
            assertEquals(1L, result.getNamespaces().get("total"));
        }

    // ✅ Non-admin user stats test
    @Test
    void testGetStats_AsNormalUser() throws CisException {
        // Admin and user mock group lists
        List<String> admins = List.of("otherAdmin");
        List<String> users = List.of("user1", "user2");

        // User groups (one namespace and one other group)
        List<String> userGroups = List.of("namespace-111", "group-role");

        // PermissionsNamespace mock
        PermissionsNamespace pns = Mockito.mock(PermissionsNamespace.class);
        Mockito.when(pns.getNamespace()).thenReturn(Integer.valueOf("111"));
        List<PermissionsNamespace> permissionsNamespaces = List.of(pns);

        // Namespace count DTO
        QueryCountByNamespaceDto dto = Mockito.mock(QueryCountByNamespaceDto.class);
        Mockito.when(dto.getNamespace()).thenReturn("111");
        Mockito.when(dto.getCount()).thenReturn(20L);
        List<QueryCountByNamespaceDto> namespaceCounts = List.of(dto);

        // Mock dependencies
        Mockito.when(crowdRequestManager.getGroupUsers("component-identifier-service-admin")).thenReturn(admins);
        Mockito.when(crowdRequestManager.getGroupUsers("component-identifier-service-user")).thenReturn(users);
        Mockito.when(authorizationService.getUserGroups(NORMAL_USERNAME)).thenReturn(userGroups);
        Mockito.when(permissionsSchemeRepository.countByUsernameIn(List.of("group-role"))).thenReturn(3L);
        Mockito.when(permissionsNamespaceRepository.findByUsernameIn(List.of("group-role"))).thenReturn(permissionsNamespaces);
        Mockito.when(sctidRepository.getCountByNamespace(List.of("111"))).thenReturn(namespaceCounts);

        // Execute service
        GetStatsResponseDto result = statsService.getStats(NORMAL_USERNAME, authDto);

        // Assert the output
        assertEquals(3L, result.getUsers()); // user1, user2, otherAdmin (total 3)
        assertEquals(3L, result.getSchemes());
        assertEquals(20L, result.getNamespaces().get("111"));
        assertEquals(1L, result.getNamespaces().get("total"));
    }

    // ❌ No groups or permissions for user
    @Test
    void testGetStats_NoGroupsNoPermissions() throws CisException {
        Mockito.when(crowdRequestManager.getGroupUsers(Mockito.anyString())).thenReturn(List.of());
        Mockito.when(authorizationService.getUserGroups(NORMAL_USERNAME)).thenReturn(List.of());

        Mockito.when(permissionsSchemeRepository.countByUsernameIn(List.of())).thenReturn(0L);
        Mockito.when(permissionsNamespaceRepository.findByUsernameIn(List.of())).thenReturn(List.of());

        GetStatsResponseDto result = statsService.getStats(NORMAL_USERNAME, authDto);

        assertEquals(0L, result.getSchemes());
        assertEquals(0L, result.getNamespaces().get("total"));
    }
}
