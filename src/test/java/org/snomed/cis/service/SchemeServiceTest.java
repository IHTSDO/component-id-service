package org.snomed.cis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.Scheme;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchemeServiceTest {

    @Mock
    private BulkSctidService bulkSctidService;

    @Mock
    private SchemeIdBaseRepository schemeIdBaseRepository;

    @Mock
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Mock
    private AuthenticateToken authenticateToken;

    @InjectMocks
    private SchemeService schemeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testIsAbleToEdit_withAdminRole_shouldReturnTrue() throws Exception {
        schemeService = new SchemeService();
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        when(mockDto.getName()).thenReturn("user1");
        when(mockDto.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        Method method = SchemeService.class.getDeclaredMethod("isAbleToEdit", String.class, AuthenticateResponseDto.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(schemeService, "SNOMEDID", mockDto);
        assertTrue(result);
    }


    @Test
    void testIsAbleToEdit_shouldReturnTrue_whenUserHasPermissionInRepo() throws Exception {
        AuthenticateResponseDto dto = mock(AuthenticateResponseDto.class);
        when(dto.getName()).thenReturn("testuser");
        when(dto.getRoles()).thenReturn(List.of("ROLE_somegroup"));
        PermissionsScheme permission = new PermissionsScheme();
        permission.setUsername("testuser");
        permission.setScheme("SNOMEDID");
        permission.setRole("manager");
        when(permissionsSchemeRepository.findByScheme("SNOMEDID")).thenReturn(List.of(permission));
        Method method = SchemeService.class.getDeclaredMethod("isAbleToEdit", String.class, AuthenticateResponseDto.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(schemeService, "SNOMEDID", dto);
        assertTrue(result);
    }

    @Test
    void testIsAbleToEdit_shouldReturnFalse_whenUserHasNoPermission() throws Exception {
        AuthenticateResponseDto dto = mock(AuthenticateResponseDto.class);
        when(dto.getName()).thenReturn("randomuser");
        when(dto.getRoles()).thenReturn(List.of("ROLE_somegroup"));
        when(permissionsSchemeRepository.findAll()).thenReturn(List.of());
        boolean result = invokeIsAbleToEdit(dto, "SNOMEDID");
        assertFalse(result);
    }

    private boolean invokeIsAbleToEdit(AuthenticateResponseDto dto, String schemeName) throws Exception {
        Method method = SchemeService.class.getDeclaredMethod("isAbleToEdit", String.class, AuthenticateResponseDto.class);
        method.setAccessible(true);
        return (boolean) method.invoke(schemeService, schemeName, dto);
    }

    @Test
    void testIsAbleToEdit_shouldReturnFalse_ifNoPermissionOrAdmin() throws Exception {
        AuthenticateResponseDto dto = mock(AuthenticateResponseDto.class);
        when(dto.getName()).thenReturn("testuser");
        when(dto.getRoles()).thenReturn(List.of("ROLE_SOMETHINGELSE"));
        when(permissionsSchemeRepository.findAll()).thenReturn(List.of());
        boolean result = invokeIsAbleToEdit(dto, "SNOMEDID");
        assertFalse(result);
    }

    @Test
    void testHasSchemePermission_shouldReturnTrue_whenUserIsManager() {
        String schemeName = "SNOMEDID";
        String username = "testuser";
        PermissionsScheme permission = new PermissionsScheme();
        permission.setScheme(schemeName);
        permission.setUsername(username);
        permission.setRole("manager");
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(List.of(permission));
        boolean result = schemeService.hasSchemePermission(schemeName, username);
        assertTrue(result);
    }

    @Test
    void testHasSchemePermission_shouldReturnFalse_whenRoleIsNotManager() {
        String schemeName = "SNOMEDID";
        String username = "testuser";
        PermissionsScheme permission = new PermissionsScheme();
        permission.setScheme(schemeName);
        permission.setUsername(username);
        permission.setRole("viewer");
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(List.of(permission));
        boolean result = schemeService.hasSchemePermission(schemeName, username);
        assertFalse(result);
    }

    @Test
    void testHasSchemePermission_shouldReturnFalse_whenUsernameMismatch() {
        String schemeName = "SNOMEDID";
        String username = "testuser";
        PermissionsScheme permission = new PermissionsScheme();
        permission.setScheme(schemeName);
        permission.setUsername("otheruser");
        permission.setRole("manager");
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(List.of(permission));
        boolean result = schemeService.hasSchemePermission(schemeName, username);
        assertFalse(result);
    }

    @Test
    void testHasSchemePermission_shouldReturnFalse_whenNoPermissionsFound() {
        String schemeName = "SNOMEDID";
        String username = "testuser";
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(Collections.emptyList());
        boolean result = schemeService.hasSchemePermission(schemeName, username);
        assertFalse(result);
    }

    @Test
    void testHasSchemePermission_shouldReturnFalse_whenSchemeNameIsFalse() {
        boolean result = schemeService.hasSchemePermission("false", "testuser");
        assertFalse(result);
    }

    @Test
    void testGetSchemesForUser_shouldDelegateToGetSchemesForUsers() throws CisException {
        AuthenticateResponseDto mockToken = mock(AuthenticateResponseDto.class);
        String username = "testuser";
        Scheme scheme1 = new Scheme();
        scheme1.setName("SNOMEDID");
        Scheme scheme2 = new Scheme();
        scheme2.setName("LOINC");
        List<Scheme> expectedSchemes = List.of(scheme1, scheme2);
        SchemeService spyService = Mockito.spy(schemeService);
        doReturn(expectedSchemes).when(spyService).getSchemesForUsers(mockToken, username);
        List<Scheme> result = spyService.getSchemesForUser(mockToken, username);
        assertEquals(expectedSchemes, result);
        verify(spyService).getSchemesForUsers(mockToken, username);
    }

    @Test
    void testGetSchemesForUser_shouldReturnEmptyList_whenNoSchemesFound() throws CisException {
        AuthenticateResponseDto mockToken = mock(AuthenticateResponseDto.class);
        String username = "testuser";
        SchemeService spyService = Mockito.spy(schemeService);
        doReturn(Collections.emptyList()).when(spyService).getSchemesForUsers(mockToken, username);
        List<Scheme> result = spyService.getSchemesForUser(mockToken, username);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(spyService).getSchemesForUsers(mockToken, username);
    }

    @Test
    void testGetSchemesForUser_shouldCallGetSchemesForUsersWithCorrectArguments() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        String user = "testuser";
        SchemeService spyService = Mockito.spy(schemeService);
        doReturn(List.of()).when(spyService).getSchemesForUsers(token, user);
        spyService.getSchemesForUser(token, user);
        verify(spyService, times(1)).getSchemesForUsers(token, user);
    }

    @Test
    void testGetSchemesForUsers_shouldReturnMappedSchemes_fromPermissions() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getRoles()).thenReturn(List.of("ROLE_admin"));
        PermissionsScheme perm1 = new PermissionsScheme();
        perm1.setScheme("SNOMEDID");
        perm1.setRole("manager");
        PermissionsScheme perm2 = new PermissionsScheme();
        perm2.setScheme("LOINC");
        perm2.setRole("viewer");
        when(permissionsSchemeRepository.findByUsername("admin")).thenReturn(List.of(perm1, perm2));
        List<Scheme> result = schemeService.getSchemesForUsers(token, "anyUser");
        assertEquals(2, result.size());
        assertEquals("SNOMEDID", result.get(0).getName());
        assertEquals("LOINC", result.get(1).getName());
    }

    @Test
    void testGetSchemesForUsers_invalidRoleFormat_shouldThrowException() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getRoles()).thenReturn(List.of("ADMIN"));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> schemeService.getSchemesForUsers(token, "anyUser"));
    }

    @Test
    void testGetSchemesForUsers_multipleUnderscores_shouldUseCorrectGroup() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getRoles()).thenReturn(List.of("ROLE_component_admin"));
        PermissionsScheme perm = new PermissionsScheme("TESTSCHEME", "component", "manager");
        when(permissionsSchemeRepository.findByUsername("component")).thenReturn(List.of(perm));
        List<Scheme> result = schemeService.getSchemesForUsers(token, "anyUser");
        assertEquals(1, result.size());
        assertEquals("TESTSCHEME", result.get(0).getName());
    }

    @Test
    void testGetSchemesForUsers_noPermissions_shouldReturnEmptyList() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getRoles()).thenReturn(List.of("ROLE_admin"));
        when(permissionsSchemeRepository.findByUsername("admin")).thenReturn(Collections.emptyList());
        List<Scheme> result = schemeService.getSchemesForUsers(token, "anyUser");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSchemesForUsers_multipleRoles_onlyFirstUsed() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getRoles()).thenReturn(List.of("ROLE_admin", "ROLE_superuser"));
        PermissionsScheme perm = new PermissionsScheme("ONLYADMIN", "admin", "manager");
        when(permissionsSchemeRepository.findByUsername("admin")).thenReturn(List.of(perm));
        List<Scheme> result = schemeService.getSchemesForUsers(token, "anyUser");
        assertEquals(1, result.size());
        assertEquals("ONLYADMIN", result.get(0).getName());
    }

    @Test
    void testGetSchemesForUsers_emptyRoles_shouldThrowException() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getRoles()).thenReturn(Collections.emptyList());
        assertThrows(IndexOutOfBoundsException.class, () -> schemeService.getSchemesForUsers(token, "user"));
    }

    @Test
    void testGetSchemes_shouldReturnAllSchemes() throws CisException {
        SchemeIdBase scheme1 = new SchemeIdBase();
        scheme1.setScheme("SNOMEDID");
        SchemeIdBase scheme2 = new SchemeIdBase();
        scheme2.setScheme("LOINC");
        List<SchemeIdBase> expected = List.of(scheme1, scheme2);
        SchemeService spyService = Mockito.spy(schemeService);
        doReturn(expected).when(spyService).getSchemesAll();
        List<SchemeIdBase> result = spyService.getSchemes();
        assertEquals(2, result.size());
        assertEquals("SNOMEDID", result.get(0).getScheme());
        assertEquals("LOINC", result.get(1).getScheme());
    }

    @Test
    void testGetSchemes_shouldReturnEmptyList() throws CisException {
        SchemeService spyService = Mockito.spy(schemeService);
        doReturn(Collections.emptyList()).when(spyService).getSchemesAll();
        List<SchemeIdBase> result = spyService.getSchemes();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSchemes_shouldThrowCisException() throws CisException {
        SchemeService spyService = Mockito.spy(schemeService);
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Error")).when(spyService).getSchemesAll();
        assertThrows(CisException.class, spyService::getSchemes);
    }

    @Test
    void testGetSchemesAll_shouldReturnData_whenRepoHasRecords() throws CisException {
        SchemeIdBase scheme1 = new SchemeIdBase();
        scheme1.setScheme("SNOMEDID");
        scheme1.setIdBase("123");
        SchemeIdBase scheme2 = new SchemeIdBase();
        scheme2.setScheme("LOINC");
        scheme2.setIdBase("456");
        List<SchemeIdBase> repoData = List.of(scheme1, scheme2);
        when(schemeIdBaseRepository.findAll()).thenReturn(repoData);
        List<SchemeIdBase> result = schemeService.getSchemesAll();
        assertEquals(2, result.size());
        assertEquals("SNOMEDID", result.get(0).getScheme());
        assertEquals("LOINC", result.get(1).getScheme());
    }

    @Test
    void testGetSchemesAll_shouldReturnEmptyList_whenRepoReturnsNull() throws CisException {
        when(schemeIdBaseRepository.findAll()).thenReturn(null);
        List<SchemeIdBase> result = schemeService.getSchemesAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSchemesAll_shouldReturnEmptyList_whenRepoReturnsEmptyList() throws CisException {
        when(schemeIdBaseRepository.findAll()).thenReturn(Collections.emptyList());
        List<SchemeIdBase> result = schemeService.getSchemesAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSchemesAll_shouldThrowCisException_whenExceptionOccurs() {
        when(schemeIdBaseRepository.findAll()).thenThrow(new RuntimeException("DB failure"));
        assertThrows(RuntimeException.class, () -> schemeService.getSchemesAll());
    }

    @Test
    void testGetScheme_shouldReturnScheme_whenFound() throws CisException {
        SchemeIdBase mockScheme = new SchemeIdBase();
        mockScheme.setScheme("SNOMEDID");
        mockScheme.setIdBase("123");
        when(schemeIdBaseRepository.findByScheme("SNOMEDID")).thenReturn(Optional.of(mockScheme));
        SchemeIdBase result = schemeService.getScheme("SNOMEDID");
        assertNotNull(result);
        assertEquals("SNOMEDID", result.getScheme());
        assertEquals("123", result.getIdBase());
    }

    @Test
    void testGetScheme_shouldReturnEmptyObject_whenNotFound() throws CisException {
        when(schemeIdBaseRepository.findByScheme("UNKNOWN")).thenReturn(Optional.empty());
        SchemeIdBase result = schemeService.getScheme("UNKNOWN");
        assertNotNull(result);
        assertNull(result.getScheme());
        assertNull(result.getIdBase());
    }


    @Test
    void testGetScheme_shouldThrowException_onError() {
        when(schemeIdBaseRepository.findByScheme("SNOMEDID")).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> schemeService.getScheme("SNOMEDID"));
    }

    @Test
    void testGetSchemeAll_shouldReturnSchemeIdBase_whenFound() throws CisException {
        String schemeName = "SNOMEDID";
        SchemeIdBase mockScheme = new SchemeIdBase();
        mockScheme.setScheme(schemeName);
        mockScheme.setIdBase("1234");
        when(schemeIdBaseRepository.findByScheme(schemeName)).thenReturn(Optional.of(mockScheme));
        SchemeIdBase result = schemeService.getSchemeAll(schemeName);
        assertNotNull(result);
        assertEquals("SNOMEDID", result.getScheme());
        assertEquals("1234", result.getIdBase());
    }

    @Test
    void testGetSchemeAll_shouldReturnEmptyObject_whenNotFound() throws CisException {
        String schemeName = "UNKNOWN";
        when(schemeIdBaseRepository.findByScheme(schemeName)).thenReturn(Optional.empty());
        SchemeIdBase result = schemeService.getSchemeAll(schemeName);
        assertNotNull(result);
        assertNull(result.getScheme());
        assertNull(result.getIdBase());
    }

    @Test
    void testUpdateScheme_shouldDelegateAndReturnValue() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.toString()).thenReturn("mockToken");
        when(token.getName()).thenReturn("testuser");
        when(token.getRoles()).thenReturn(List.of("ROLE_admin_group1"));
        SchemeName schemeName = SchemeName.SNOMEDID;
        String schemeSeq = "12345";
        SchemeService spyService = Mockito.spy(schemeService);
        doReturn("Success").when(spyService).updateSchemes(token, schemeName, schemeSeq);
        String result = spyService.updateScheme(token, schemeName, schemeSeq);
        assertEquals("Success", result);
        verify(spyService).updateSchemes(token, schemeName, schemeSeq);
    }

    @Test
    void testUpdateScheme_shouldThrowException_whenTokenIsNull() {
        SchemeName schemeName = SchemeName.SNOMEDID;
        String schemeSeq = "12345";
        assertThrows(NullPointerException.class, () -> {
            schemeService.updateScheme(null, schemeName, schemeSeq);
        });
    }

    @Test
    void testUpdateScheme_shouldHandleEmptySchemeSeq() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("testuser");
        when(token.getRoles()).thenReturn(List.of("ROLE_admin_group1"));
        SchemeName schemeName = SchemeName.SNOMEDID;
        String schemeSeq = "";
        SchemeService spyService = Mockito.spy(schemeService);
        doReturn("Empty schemeSeq handled").when(spyService).updateSchemes(token, schemeName, schemeSeq);
        String result = spyService.updateScheme(token, schemeName, schemeSeq);
        assertEquals("Empty schemeSeq handled", result);
    }

    @Test
    void testUpdateScheme_shouldThrowCisException_whenUpdateFails() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("testuser");
        when(token.getRoles()).thenReturn(List.of("ROLE_admin_group1"));
        SchemeName schemeName = SchemeName.SNOMEDID;
        String schemeSeq = "12345";
        SchemeService spyService = Mockito.spy(schemeService);
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Update failed")).when(spyService).updateSchemes(token, schemeName, schemeSeq);
        CisException ex = assertThrows(CisException.class, () -> {
            spyService.updateScheme(token, schemeName, schemeSeq);
        });
        assertEquals("Update failed", ex.getMessage());
    }

    @Test
    void testUpdateSchemes_shouldUpdateAndReturnSuccess() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("testuser");
        when(token.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        SchemeName schemeName = SchemeName.SNOMEDID;
        String newSchemeSeq = "newSeq";
        SchemeIdBase schemeIdBase = new SchemeIdBase();
        schemeIdBase.setScheme(schemeName.toString());
        schemeIdBase.setIdBase("oldSeq");
        when(schemeIdBaseRepository.findByScheme(schemeName.toString())).thenReturn(Optional.of(schemeIdBase));
        when(schemeIdBaseRepository.save(any())).thenReturn(schemeIdBase);
        String response = schemeService.updateSchemes(token, schemeName, newSchemeSeq);
        assertNotNull(response);
        assertTrue(response.contains("Success"));
        verify(schemeIdBaseRepository).save(argThat(sib -> sib.getIdBase().equals(newSchemeSeq)));
    }

    @Test
    void testUpdateSchemes_shouldThrowException_ifNoPermission() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("testuser");
        when(token.getRoles()).thenReturn(List.of("ROLE_somegroup"));
        SchemeName schemeName = SchemeName.SNOMEDID;
        String newSchemeSeq = "newSeq";
        when(permissionsSchemeRepository.findByScheme(schemeName.toString())).thenReturn(Collections.emptyList());
        CisException ex = assertThrows(CisException.class, () -> {
            schemeService.updateSchemes(token, schemeName, newSchemeSeq);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getErrorMessage());
    }

    @Test
    void testUpdateSchemes_shouldReturnNull_ifSchemeNotFound() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("testuser");
        when(token.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        SchemeName schemeName = SchemeName.SNOMEDID;
        String newSchemeSeq = "newSeq";
        when(schemeIdBaseRepository.findByScheme(schemeName.toString())).thenReturn(Optional.empty());
        String response = schemeService.updateSchemes(token, schemeName, newSchemeSeq);
        assertNull(response);
        verify(schemeIdBaseRepository, never()).save(any());
    }

    @Test
    void testGetPermissionsForScheme_shouldReturnPermissionsList() {
        String schemeName = "SNOMEDID";
        PermissionsScheme permission1 = new PermissionsScheme();
        permission1.setScheme(schemeName);
        permission1.setUsername("user1");
        permission1.setRole("manager");
        PermissionsScheme permission2 = new PermissionsScheme();
        permission2.setScheme(schemeName);
        permission2.setUsername("user2");
        permission2.setRole("viewer");
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(List.of(permission1, permission2));
        List<PermissionsScheme> result = schemeService.getPermissionsForScheme(schemeName);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
    }

    @Test
    void testGetPermissionsForScheme_shouldReturnEmptyList_whenNoPermissions() {
        String schemeName = "UNKNOWN";
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(Collections.emptyList());
        List<PermissionsScheme> result = schemeService.getPermissionsForScheme(schemeName);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPermissionsForScheme_shouldHandleNullReturnGracefully() {
        String schemeName = "SNOMEDID";
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(null); // Simulate null return
        List<PermissionsScheme> result = schemeService.getPermissionsForScheme(schemeName);
        assertTrue(result == null || result.isEmpty());
    }

    @Test
    void testGetPermissionsForScheme_shouldCallRepositoryWithCorrectScheme() {
        String schemeName = "SNOMEDID";
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(Collections.emptyList());
        schemeService.getPermissionsForScheme(schemeName);
        verify(permissionsSchemeRepository).findByScheme(schemeName);
    }

    @Test
    void testGetPermissionsForScheme_shouldReturnEmptyList_whenSchemeIsNull() {
        List<PermissionsScheme> result = schemeService.getPermissionsForScheme(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPermissionsForScheme_shouldReturnEmptyList_whenSchemeIsEmpty() {
        List<PermissionsScheme> result = schemeService.getPermissionsForScheme("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteSchemePermissions_shouldReturnSuccess_whenUserHasPermission() throws Exception {
        String schemeName = "SNOMEDID";
        String username = "testuser";
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn(username);
        when(token.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        String response = schemeService.deleteSchemePermissions(schemeName, username, token);
        assertNotNull(response);
        assertTrue(response.contains("Success"));
        verify(permissionsSchemeRepository).deleteBySchemeAndUsername(schemeName, username);
    }

    @Test
    void testDeleteSchemePermissions_shouldThrowException_whenUserHasNoPermission() {
        String schemeName = "SNOMEDID";
        String username = "testuser";
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn(username);
        when(token.getRoles()).thenReturn(List.of("ROLE_somegroup"));
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(Collections.emptyList());
        CisException ex = assertThrows(CisException.class, () -> {
            schemeService.deleteSchemePermissions(schemeName, username, token);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getErrorMessage());
        verify(permissionsSchemeRepository, never()).deleteBySchemeAndUsername(anyString(), anyString());
    }

    @Test
    void testCreateSchemePermissions_success() throws Exception {
        String schemeName = "SNOMEDID";
        String userName = "testuser";
        String role = "manager";
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn(userName);
        when(token.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        when(permissionsSchemeRepository.findBySchemeAndUsernameAndRole(schemeName, userName, role)).thenReturn(Optional.empty()); // No duplicate
        PermissionsScheme savedPermission = PermissionsScheme.builder().scheme(schemeName).username(userName).role(role).build();
        when(permissionsSchemeRepository.save(any(PermissionsScheme.class))).thenReturn(savedPermission);
        String response = schemeService.createSchemePermissions(schemeName, userName, role, token);
        assertNotNull(response);
        assertTrue(response.contains("Success"));
        verify(permissionsSchemeRepository).save(any(PermissionsScheme.class));
    }

    @Test
    void testCreateSchemePermissions_shouldThrowDuplicateException() {
        String schemeName = "SNOMEDID";
        String userName = "testuser";
        String role = "manager";
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn(userName);
        when(token.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        PermissionsScheme existingPermission = PermissionsScheme.builder().scheme(schemeName).username(userName).role(role).build();
        when(permissionsSchemeRepository.findBySchemeAndUsernameAndRole(schemeName, userName, role)).thenReturn(Optional.of(existingPermission));
        CisException ex = assertThrows(CisException.class, () -> {
            schemeService.createSchemePermissions(schemeName, userName, role, token);
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getErrorMessage().contains("ER_DUP_ENTRY"));
        verify(permissionsSchemeRepository, never()).save(any());
    }

    @Test
    void testCreateSchemePermissions_shouldThrowUnauthorized() {
        String schemeName = "SNOMEDID";
        String userName = "testuser";
        String role = "manager";
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn(userName);
        when(token.getRoles()).thenReturn(List.of("ROLE_somegroup"));
        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(Collections.emptyList());
        CisException ex = assertThrows(CisException.class, () -> {
            schemeService.createSchemePermissions(schemeName, userName, role, token);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getErrorMessage());
        verify(permissionsSchemeRepository, never()).save(any());
    }
}