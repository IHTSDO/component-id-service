package org.snomed.cis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.domain.Namespace;
import org.snomed.cis.domain.Partitions;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.NamespaceDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.NamespaceRepository;
import org.snomed.cis.repository.PartitionsRepository;
import org.snomed.cis.repository.PermissionsNamespaceRepository;
import org.snomed.cis.util.ImsRequestManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamespaceServiceTest {
    @Spy
    @InjectMocks
    private NamespaceService namespaceService;

    @Mock
    private NamespaceRepository namespaceRepository;

    @Mock
    private PartitionsRepository partitionsRepository;
    @Mock
    private PermissionsNamespaceRepository permissionsNamespaceRepository;

    @Mock
    private ImsRequestManager imsRequestManager;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(namespaceService, "namespaceRepository", namespaceRepository);
        ReflectionTestUtils.setField(namespaceService, "partitionsRepository", partitionsRepository);
        ReflectionTestUtils.setField(namespaceService, "permissionsNamespaceRepository", permissionsNamespaceRepository);
        ReflectionTestUtils.setField(namespaceService, "imsRequestManager", imsRequestManager);

    }

    @Test
    void testGetNamespaceslist_ReturnsCorrectlyMappedAndSortedList() {
        Namespace ns1 = new Namespace();
        ns1.setNamespace(200);
        ns1.setOrganizationName("Org B");

        ns1.setEmail("b@org.com");
        ns1.setNotes("Note B");
        ns1.setDateIssued(LocalDate.of(2020, 1, 1).atStartOfDay());
        ns1.setIdPregenerate(String.valueOf(true));
        Namespace ns2 = new Namespace();
        ns2.setNamespace(100);
        ns2.setOrganizationName("Org A");
        ns2.setEmail("a@org.com");
        ns2.setNotes("Note A");
        ns2.setDateIssued(LocalDate.of(2021, 1, 1).atStartOfDay());
        ns2.setIdPregenerate(String.valueOf(false));
        Partitions p1 = new Partitions();
        p1.setNamespace(100);
        Partitions p2 = new Partitions();
        p2.setNamespace(200);
        when(namespaceRepository.findAll()).thenReturn(Arrays.asList(ns1, ns2));
        when(partitionsRepository.findAll()).thenReturn(Arrays.asList(p1, p2));
        List<NamespaceDto> result = namespaceService.getNamespaceslist();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals((Integer) 100, result.get(0).getNamespace());
        assertEquals((Integer) 200, result.get(1).getNamespace());
        assertEquals(1, result.get(0).getPartitions().size());
        assertEquals(1, result.get(1).getPartitions().size());
    }

    @Test
    void testGetNamespaceslist_WhenEmpty_ReturnsEmptyList() {
        when(namespaceRepository.findAll()).thenReturn(Collections.emptyList());
        when(partitionsRepository.findAll()).thenReturn(Collections.emptyList());
        List<NamespaceDto> result = namespaceService.getNamespaceslist();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateNamespace_DelegatesToCreateNamespaces() throws Exception {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        NamespaceDto mockNamespaceDto = new NamespaceDto();
        String expectedResponse = "namespace-created-successfully";
        doReturn(expectedResponse).when(namespaceService).createNamespaces(mockAuthDto, mockNamespaceDto);
        String result = namespaceService.createNamespace(mockAuthDto, mockNamespaceDto);
        assertEquals(expectedResponse, result);
        verify(namespaceService, times(1)).createNamespaces(mockAuthDto, mockNamespaceDto);
    }

    @Test
    void testCreateNamespace_WhenCreateNamespacesReturnsNull() throws Exception {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        NamespaceDto mockNamespaceDto = new NamespaceDto();
        doReturn(null).when(namespaceService).createNamespaces(mockAuthDto, mockNamespaceDto);
        String result = namespaceService.createNamespace(mockAuthDto, mockNamespaceDto);
        assertNull(result);
        verify(namespaceService, times(1)).createNamespaces(mockAuthDto, mockNamespaceDto);
    }

    @Test
    void testCreateNamespaces_WithValidNamespaceAndPermission() throws Exception {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        NamespaceDto inputDto = new NamespaceDto();
        inputDto.setNamespace(1234567);
        doReturn(true).when(namespaceService).isAbleToEdit(1234567, mockAuthDto);
        doReturn("Namespace Created").when(namespaceService).createNamespaceList(inputDto);
        String result = namespaceService.createNamespaces(mockAuthDto, inputDto);
        assertEquals("Namespace Created", result);
        verify(namespaceService).isAbleToEdit(1234567, mockAuthDto);
        verify(namespaceService).createNamespaceList(inputDto);
    }

    @Test
    void testCreateNamespaces_WithInvalidNamespace_ThrowsBadRequest() {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        NamespaceDto inputDto = new NamespaceDto();
        inputDto.setNamespace(1234);
        doReturn(true).when(namespaceService).isAbleToEdit(1234, mockAuthDto);
        CisException ex = assertThrows(CisException.class, () -> namespaceService.createNamespaces(mockAuthDto, inputDto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Invalid namespace", ex.getMessage());
    }

    @Test
    void testCreateNamespaces_WhenNotAllowedToEdit_ThrowsUnauthorized()  {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        NamespaceDto inputDto = new NamespaceDto();
        inputDto.setNamespace(1234567);
        doReturn(false).when(namespaceService).isAbleToEdit(1234567, mockAuthDto);
        CisException ex = assertThrows(CisException.class, () -> namespaceService.createNamespaces(mockAuthDto, inputDto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getMessage());
    }

    @Test
    void testCreateNamespaceList_WithNonZeroNamespace_CreatesProperPartitionsAndSavesNamespace() {
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(1234567);
        namespaceDto.setOrganizationName("Test Org");
        namespaceDto.setDateIssued("10-07-2025 10:30:00 AM");
        namespaceDto.setEmail("test@org.com");
        namespaceDto.setNotes("Test Notes");
        namespaceDto.setIdPregenerate("YES");
        List<Partitions> expectedPartitions = List.of(new Partitions(1234567, "10", 0), new Partitions(1234567, "11", 0), new Partitions(1234567, "12", 0));
        Namespace savedNamespace = new Namespace();
        savedNamespace.setNamespace(1234567);
        when(partitionsRepository.saveAll(anyList())).thenReturn(expectedPartitions);
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(savedNamespace);
        String result = null;
        result = namespaceService.createNamespaceList(namespaceDto);
        assertNotNull(result);
        assertTrue(result.contains("Success"));
        verify(partitionsRepository).saveAll(anyList());
        verify(namespaceRepository).save(any(Namespace.class));
    }

    @Test
    void testCreateNamespaceList_WithZeroNamespace_Creates00To02Partitions() {
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(0);
        namespaceDto.setOrganizationName("Zero Org");
        namespaceDto.setDateIssued("10-07-2025 11:00:00 AM");
        namespaceDto.setEmail("zero@org.com");
        namespaceDto.setNotes("Zero Notes");
        namespaceDto.setIdPregenerate("YES");
        List<Partitions> expectedPartitions = List.of(new Partitions(0, "00", 0), new Partitions(0, "01", 0), new Partitions(0, "02", 0));
        Namespace savedNamespace = new Namespace();
        savedNamespace.setNamespace(0);
        when(partitionsRepository.saveAll(anyList())).thenReturn(expectedPartitions);
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(savedNamespace);
        String result = assertDoesNotThrow(() -> namespaceService.createNamespaceList(namespaceDto));
        assertNotNull(result);
        assertTrue(result.contains("Success"));
        ArgumentCaptor<List<Partitions>> captor = ArgumentCaptor.forClass(List.class);
        verify(partitionsRepository).saveAll(captor.capture());
        List<Partitions> actualPartitions = captor.getValue();
        assertEquals(3, actualPartitions.size());
        assertTrue(actualPartitions.stream().anyMatch(p -> p.getPartitionId().equals("00")));
        assertTrue(actualPartitions.stream().anyMatch(p -> p.getPartitionId().equals("01")));
        assertTrue(actualPartitions.stream().anyMatch(p -> p.getPartitionId().equals("02")));
        verify(namespaceRepository).save(any(Namespace.class));
    }

    @Test
    void testIsAbleToEdit_WithAdminRole_ReturnsTrue() {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        when(mockAuthDto.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        boolean result = namespaceService.isAbleToEdit(1234567, mockAuthDto);
        assertTrue(result);
    }

    @Test
    void testIsAbleToEdit_WithNamespacePermission_ReturnsTrue() {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        when(mockAuthDto.getRoles()).thenReturn(List.of("ROLE_something-else"));
        when(mockAuthDto.getName()).thenReturn("testUser");
        when(namespaceService.hasNamespacePermission(1234567, "testUser")).thenReturn(true);
        boolean result = namespaceService.isAbleToEdit(1234567, mockAuthDto);
        assertTrue(result);
    }

    @Test
    void testIsAbleToEdit_WithoutAdminOrPermission_ReturnsFalse() {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        when(mockAuthDto.getRoles()).thenReturn(List.of("ROLE_random-role"));
        when(mockAuthDto.getName()).thenReturn("testUser");
        when(namespaceService.hasNamespacePermission(1234567, "testUser")).thenReturn(false);
        boolean result = namespaceService.isAbleToEdit(1234567, mockAuthDto);
        assertFalse(result);
    }

    @Test
    void testHasNamespacePermission_WithMatchingManagerRole_ReturnsTrue() {
        Integer namespace = 1234567;
        String username = "testUser";
        PermissionsNamespace managerPermission = new PermissionsNamespace();
        managerPermission.setNamespace(namespace);
        managerPermission.setRole("manager");
        managerPermission.setUsername("testUser");
        List<PermissionsNamespace> permissionList = List.of(managerPermission);
        when(permissionsNamespaceRepository.findByNamespace(namespace)).thenReturn(permissionList);
        boolean result = namespaceService.hasNamespacePermission(namespace, username);
        assertTrue(result);
        verify(permissionsNamespaceRepository).findByNamespace(namespace);
    }

    @Test
    void testHasNamespacePermission_WithNoMatchingPermission_ReturnsFalse() {
        Integer namespace = 1234567;
        String username = "testUser";
        PermissionsNamespace otherPermission = new PermissionsNamespace();
        otherPermission.setNamespace(namespace);
        otherPermission.setRole("viewer");
        otherPermission.setUsername("anotherUser");
        when(permissionsNamespaceRepository.findByNamespace(namespace)).thenReturn(List.of(otherPermission));
        boolean result = namespaceService.hasNamespacePermission(namespace, username);
        assertFalse(result);
        verify(permissionsNamespaceRepository).findByNamespace(namespace);
    }

    @Test
    void testHasNamespacePermission_WithNoMatchingUsername_ReturnsFalse() {
        Integer namespace = 1234567;
        String username = "testUser";
        PermissionsNamespace permission = new PermissionsNamespace();
        permission.setNamespace(namespace);
        permission.setRole("manager");
        permission.setUsername("anotherUser");
        when(permissionsNamespaceRepository.findByNamespace(namespace)).thenReturn(List.of(permission));
        boolean result = namespaceService.hasNamespacePermission(namespace, username);
        assertFalse(result);
        verify(permissionsNamespaceRepository).findByNamespace(namespace);
    }

    @Test
    void testUpdateNamespace_CallsUpdateNamespacesAndReturnsResult() throws CisException {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(1234567);
        namespaceDto.setOrganizationName("Test Org");
        doReturn("Update Success").when(namespaceService).updateNamespaces(mockAuthDto, namespaceDto);
        String result = namespaceService.updateNamespace(mockAuthDto, namespaceDto);
        assertEquals("Update Success", result);
        verify(namespaceService).updateNamespaces(mockAuthDto, namespaceDto);
    }

    @Test
    void testUpdateNamespace_Success_ReturnsResult() throws CisException {
        AuthenticateResponseDto authDto = mock(AuthenticateResponseDto.class);
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(1234567);
        doReturn("Update Success").when(namespaceService).updateNamespaces(authDto, namespaceDto);
        String result = namespaceService.updateNamespace(authDto, namespaceDto);
        assertEquals("Update Success", result);
    }

    @Test
    void testUpdateNamespace_WhenUpdateThrowsCisException_PropagatesException() throws CisException {
        AuthenticateResponseDto authDto = mock(AuthenticateResponseDto.class);
        NamespaceDto namespaceDto = new NamespaceDto();
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Update failed")).when(namespaceService).updateNamespaces(authDto, namespaceDto);
        CisException exception = assertThrows(CisException.class, () -> namespaceService.updateNamespace(authDto, namespaceDto));
        assertEquals("Update failed", exception.getMessage());
    }

    @Test
    void testUpdateNamespace_NullNamespaceDto_ThrowsException() {
        AuthenticateResponseDto authDto = mock(AuthenticateResponseDto.class);
        assertThrows(NullPointerException.class, () -> namespaceService.updateNamespace(authDto, null));
    }

    @Test
    void testUpdateNamespace_WhenUserLacksPermission_ThrowsCisException(){
        AuthenticateResponseDto authDto = mock(AuthenticateResponseDto.class);
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(1234567);
        doReturn(false).when(namespaceService).isAbleToEdit(1234567, authDto);
        CisException exception = assertThrows(CisException.class, () -> namespaceService.updateNamespaces(authDto, namespaceDto));
        assertEquals("No permission for the selected operation", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void testUpdateNamespace_WhenUserHasPermission_ReturnsSuccess() throws CisException {
        AuthenticateResponseDto authDto = mock(AuthenticateResponseDto.class);
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(1234567);
        doReturn(true).when(namespaceService).isAbleToEdit(1234567, authDto);
        doReturn("Update Successful").when(namespaceService).editNamespace(1234567, namespaceDto);
        String result = namespaceService.updateNamespaces(authDto, namespaceDto);
        assertEquals("Update Successful", result);
    }

    @Test
    void testEditNamespace_WhenNamespaceFound_SuccessfullyUpdates() throws CisException {
        int namespaceId = 123;
        NamespaceDto dto = new NamespaceDto();
        dto.setOrganizationName("New Org");
        dto.setDateIssued("10-7-2025 05:30:00 PM");
        dto.setEmail("email@example.com");
        dto.setNotes("notes");
        dto.setIdPregenerate(String.valueOf(true));
        Namespace existingNamespace = new Namespace();
        existingNamespace.setNamespace(namespaceId);
        when(namespaceRepository.findById(namespaceId)).thenReturn(Optional.of(existingNamespace));
        when(namespaceRepository.save(any())).thenReturn(existingNamespace);
        String response = namespaceService.editNamespace(namespaceId, dto);
        assertEquals("{\"message\":\"Success\"}", response);
        verify(namespaceRepository).save(any());
    }

    @Test
    void testGetNamespacesForUser_ReturnsNamespaceList() throws CisException {
        // Arrange
        String userName = "testUser";
        List<Namespace> mockList = List.of(new Namespace(), new Namespace());

        ImsRequestManager imsRequest = Mockito.mock(ImsRequestManager.class);
        NamespaceService service = Mockito.spy(new NamespaceService(imsRequest));

        doReturn(mockList).when(service).getNamespacesListForUser("dummy-token", userName);

        // Act
        List<Namespace> result = service.getNamespacesForUser("dummy-token", userName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(service).getNamespacesListForUser("dummy-token", userName);
    }

    @Test
    void testGetNamespacesForUser_ThrowsCisException() throws CisException {
        String userName = "errorUser";
        ImsRequestManager imsRequestManager = Mockito.mock(ImsRequestManager.class);
        NamespaceService service = Mockito.spy(new NamespaceService(imsRequestManager));
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed")).when(service).getNamespacesListForUser("dummy-token",userName);
        CisException exception = assertThrows(CisException.class, () -> service.getNamespacesForUser("dummy-token",userName));
        assertEquals("Failed", exception.getMessage());
    }

    @Test
    void testGetNamespacesForUser_ShouldReturnFilteredAndSortedNamespaces() throws CisException {
        String userName = "testUser";
        List<String> mockGroups = List.of("namespace-1002", "namespace-1001");
        when(imsRequestManager.getUserGroups("dummy-token",userName)).thenReturn(mockGroups);
        when(permissionsNamespaceRepository.findByUsernameIn(anyList())).thenReturn(Collections.emptyList());
        Namespace ns1 = new Namespace();
        ns1.setNamespace(1001);
        Namespace ns2 = new Namespace();
        ns2.setNamespace(1002);
        List<Namespace> mockNamespaceList = new ArrayList<>();
        mockNamespaceList.add(ns2);
        mockNamespaceList.add(ns1);
        when(namespaceRepository.findByNamespaceIn(Arrays.asList(1002, 1001))).thenReturn(mockNamespaceList);
        List<Namespace> result = namespaceService.getNamespacesForUser("dummy-token",userName);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals((Integer) 1001, result.get(0).getNamespace());
        assertEquals((Integer) 1002, result.get(1).getNamespace());
        verify(imsRequestManager).getUserGroups("dummy-token",userName);
        verify(permissionsNamespaceRepository).findByUsernameIn(anyList());
        verify(namespaceRepository).findByNamespaceIn(Arrays.asList(1002, 1001));
    }

    @Test
    void testGetNamespacesForUser_WhenOnlyPermissionNamespacesExist_ShouldReturnSortedList() throws CisException {
        String userName = "testUser";
        List<String> mockGroups = List.of("admin-group", "qa-team");
        when(imsRequestManager.getUserGroups("dummy-token",userName)).thenReturn(mockGroups);
        PermissionsNamespace perm1 = new PermissionsNamespace();
        perm1.setUsername("admin-group");
        perm1.setNamespace(1003);
        PermissionsNamespace perm2 = new PermissionsNamespace();
        perm2.setUsername("qa-team");
        perm2.setNamespace(1002);
        List<PermissionsNamespace> permissions = Arrays.asList(perm1, perm2);
        when(permissionsNamespaceRepository.findByUsernameIn(Arrays.asList("admin-group", "qa-team", userName))).thenReturn(permissions);
        Namespace ns2 = new Namespace();
        ns2.setNamespace(1002);
        Namespace ns3 = new Namespace();
        ns3.setNamespace(1003);
        List<Namespace> namespaceList = Arrays.asList(ns3, ns2);
        when(namespaceRepository.findByNamespaceIn(Arrays.asList(1003, 1002))).thenReturn(namespaceList);
        List<Namespace> result = namespaceService.getNamespacesForUser("dummy-token",userName);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals((Integer) 1002, result.get(0).getNamespace());
        assertEquals((Integer) 1003, result.get(1).getNamespace());
        verify(imsRequestManager).getUserGroups("dummy-token",userName);
        verify(permissionsNamespaceRepository).findByUsernameIn(Arrays.asList("admin-group", "qa-team", userName));
        verify(namespaceRepository).findByNamespaceIn(Arrays.asList(1003, 1002));
    }

    @Test
    void testConvertStringListToIntList_ShouldConvertProperly() {
        List<String> input = Arrays.asList("10", "20", "30");
        List<Integer> result = namespaceService.convertStringListToIntList(input, Integer::parseInt);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(List.of(10, 20, 30), result);
    }

    @Test
    void testConvertStringListToIntList_WithEmptyList_ShouldReturnEmptyList() {
        List<String> input = List.of();
        List<Integer> result = namespaceService.convertStringListToIntList(input, Integer::parseInt);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertStringListToIntList_WithInvalidInput_ShouldThrowException() {
        List<String> input = List.of("10", "abc");
        assertThrows(NumberFormatException.class, () -> namespaceService.convertStringListToIntList(input, Integer::parseInt));
    }

    @Test
    void testGetNamespace_WithValidId_ShouldReturnDto() throws CisException {
        NamespaceDto mockDto = new NamespaceDto();
        mockDto.setNamespace(1001);
        Mockito.doReturn(mockDto).when(namespaceService).getNamespaceId("1001");
        NamespaceDto result = namespaceService.getNamespace("1001");
        assertNotNull(result);
        assertEquals((Integer) 1001, result.getNamespace());
    }

    @Test
    void testGetNamespace_WithUndefinedId_ShouldReturnDefaultDto() throws CisException {
        NamespaceDto mockDto = new NamespaceDto();
        mockDto.setNamespace(0);
        Mockito.doReturn(mockDto).when(namespaceService).getNamespaceId("0");
        NamespaceDto result = namespaceService.getNamespace("undefined");
        assertNotNull(result);
        assertEquals((Integer) 0, result.getNamespace());
    }


    @Test
    void testGetNamespace_WithInvalidId_ShouldThrowException() throws CisException {
        Mockito.doThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid namespace")).when(namespaceService).getNamespaceId("9999");
        CisException ex = assertThrows(CisException.class, () -> namespaceService.getNamespace("9999"));
        assertEquals("Invalid namespace", ex.getMessage());
    }

    @Test
    void testGetNamespaceId_WithValidId_ShouldReturnDto() throws Exception {
        Namespace mockNamespace = new Namespace();
        mockNamespace.setNamespace(1234567);
        mockNamespace.setOrganizationName("Org");
        mockNamespace.setDateIssued(LocalDate.now().atStartOfDay());
        mockNamespace.setEmail("email@test.com");
        mockNamespace.setNotes("Note");
        mockNamespace.setIdPregenerate(String.valueOf(true));
        List<Partitions> mockPartitions = List.of(new Partitions(1234567, "ABC", 1));
        Mockito.when(namespaceRepository.findById(1234567)).thenReturn(Optional.of(mockNamespace));
        Mockito.when(partitionsRepository.findByNamespace(1234567)).thenReturn(mockPartitions);
        NamespaceDto result = namespaceService.getNamespaceId("1234567");
        assertNotNull(result);
        assertEquals(1234567, (int) result.getNamespace());
        assertEquals("Org", result.getOrganizationName());
        assertEquals(1, result.getPartitions().size());
    }

    @Test
    void testGetNamespaceId_WithZeroId_ShouldReturnDto() throws Exception {
        Namespace mockNamespace = new Namespace();
        mockNamespace.setNamespace(0);
        mockNamespace.setOrganizationName("Default Org");
        List<Partitions> mockPartitions = new ArrayList<>();
        Mockito.when(namespaceRepository.findById(0)).thenReturn(Optional.of(mockNamespace));
        Mockito.when(partitionsRepository.findByNamespace(0)).thenReturn(mockPartitions);
        NamespaceDto result = namespaceService.getNamespaceId("0");
        assertNotNull(result);
        assertEquals(0, (int) result.getNamespace());
        assertEquals("Default Org", result.getOrganizationName());
    }

    @Test
    void testGetNamespaceId_WithInvalidId_ShouldThrowException() {
        CisException thrown = assertThrows(CisException.class, () -> namespaceService.getNamespaceId("999"),
                "Expected getNamespaceId() to throw, but it didn't");
        assertEquals("Invalid namespace", thrown.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatus());
    }

    @Test
    void testDeleteNamespace_ShouldReturnSuccessMessage() throws CisException {
        AuthenticateResponseDto mockAuthDto = Mockito.mock(AuthenticateResponseDto.class);
        String namespaceId = "1234567";
        String expectedResponse = "Namespace deleted successfully";
        Mockito.doReturn(expectedResponse).when(namespaceService).deleteNamespaces(mockAuthDto, namespaceId);
        String result = namespaceService.deleteNamespace(mockAuthDto, namespaceId);
        assertEquals(expectedResponse, result);
        Mockito.verify(namespaceService).deleteNamespaces(mockAuthDto, namespaceId);
    }

    @Test
    void testDeleteNamespace_ShouldThrowCisException_WhenDeleteFails() throws CisException {
        AuthenticateResponseDto mockAuthDto = Mockito.mock(AuthenticateResponseDto.class);
        String namespaceId = "1234567";
        CisException exception = new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete");
        Mockito.doThrow(exception).when(namespaceService).deleteNamespaces(mockAuthDto, namespaceId);
        CisException thrown = assertThrows(CisException.class, () -> namespaceService.deleteNamespace(mockAuthDto, namespaceId));
        assertEquals("Failed to delete", thrown.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatus());
        Mockito.verify(namespaceService).deleteNamespaces(mockAuthDto, namespaceId);
    }

    @Test
    void testDeleteNamespaces_ShouldReturnInvalidNamespaceMessage_WhenNamespaceIdInvalidLength() throws CisException {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        String namespaceId = "123";
        String response = namespaceService.deleteNamespaces(mockAuthDto, namespaceId);
        assertTrue(response.contains("Invalid Namespace"));
        verifyNoInteractions(namespaceRepository);
        verifyNoInteractions(partitionsRepository);
    }

    @Test
    void testUpdatePartitionSequence_ShouldReturnUpdatedValue() throws CisException {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        String namespaceId = "1000001";
        String partitionId = "01";
        String value = "123456";
        String expectedResponse = "Partition sequence updated successfully";

        ImsRequestManager imsRequest = Mockito.mock(ImsRequestManager.class);

        NamespaceService spyService = Mockito.spy(new NamespaceService(imsRequest));

        spyService.namespaceRepository = namespaceRepository;
        spyService.partitionsRepository = partitionsRepository;
        spyService.permissionsNamespaceRepository = permissionsNamespaceRepository;

        Mockito.doReturn(expectedResponse)
                .when(spyService).updatePartitionSequences(mockAuthDto, namespaceId, partitionId, value);

        String actualResponse = spyService.updatePartitionSequence(mockAuthDto, namespaceId, partitionId, value);

        assertEquals(expectedResponse, actualResponse);
        Mockito.verify(spyService).updatePartitionSequences(mockAuthDto, namespaceId, partitionId, value);
    }


    @Test
    void testUpdatePartitionSequence_ShouldThrowException_WhenUserIsNotAuthorized() throws CisException {
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        String namespaceId = "1000001";
        String partitionId = "01";
        String value = "123456";

        ImsRequestManager imsRequest = Mockito.mock(ImsRequestManager.class);

        NamespaceService spyService = Mockito.spy(new NamespaceService(imsRequest));

        spyService.namespaceRepository = namespaceRepository;
        spyService.partitionsRepository = partitionsRepository;
        spyService.permissionsNamespaceRepository = permissionsNamespaceRepository;

        Mockito.doThrow(new CisException(HttpStatus.UNAUTHORIZED, "No permission"))
                .when(spyService).updatePartitionSequences(mockAuthDto, namespaceId, partitionId, value);

        CisException exception = assertThrows(CisException.class, () -> {
            spyService.updatePartitionSequence(mockAuthDto, namespaceId, partitionId, value);
        });

        assertEquals("No permission", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        Mockito.verify(spyService).updatePartitionSequences(mockAuthDto, namespaceId, partitionId, value);
    }


    @Test
    void testUpdatePartitionSequences_ShouldReturnSuccess_WhenUserHasPermissionAndPartitionExists() throws Exception {
        String namespaceId = "1000001";
        String partitionId = "01";
        String value = "123";
        AuthenticateResponseDto authDto = mock(AuthenticateResponseDto.class);

        Partitions existingPartition = new Partitions();
        existingPartition.setNamespace(Integer.valueOf(namespaceId));
        existingPartition.setPartitionId(partitionId);
        existingPartition.setSequence(0);

        ImsRequestManager imsRequest = Mockito.mock(ImsRequestManager.class);

        NamespaceService spyService = Mockito.spy(new NamespaceService(imsRequest));

        spyService.partitionsRepository = Mockito.mock(PartitionsRepository.class);
        spyService.permissionsNamespaceRepository = Mockito.mock(PermissionsNamespaceRepository.class);

        // Stub behavior
        Mockito.doReturn(true).when(spyService).isAbleToEdit(Integer.valueOf(namespaceId), authDto);

        Mockito.when(spyService.partitionsRepository.findById(Mockito.argThat(
                p -> p.getNamespace().equals(Integer.valueOf(namespaceId)) &&
                        p.getPartitionId().equals(partitionId)
        ))).thenReturn(Optional.of(existingPartition));

        Mockito.when(spyService.partitionsRepository.save(Mockito.any(Partitions.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = spyService.updatePartitionSequences(authDto, namespaceId, partitionId, value);

        // Assert
        assertTrue(result.contains("Success"));
        Mockito.verify(spyService).isAbleToEdit(Integer.valueOf(namespaceId), authDto);
        Mockito.verify(spyService.partitionsRepository).findById(Mockito.argThat(
                p -> p.getNamespace().equals(Integer.valueOf(namespaceId)) &&
                        p.getPartitionId().equals(partitionId)
        ));
        Mockito.verify(spyService.partitionsRepository).save(existingPartition);
    }

    @Test
    void testDeleteNamespacePermissionsOfUser_ShouldThrowCisException_WhenUserHasNoPermission() {
        String namespaceId = "1000001";
        String username = "testuser";
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        doReturn(false).when(namespaceService).isAbleToEdit(Integer.parseInt(namespaceId), mockAuthDto);
        CisException ex = assertThrows(CisException.class, () -> namespaceService.deleteNamespacePermissionsOfUser(namespaceId, username, mockAuthDto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        verify(permissionsNamespaceRepository, never()).deleteByNamespaceAndUsername(anyInt(), anyString());
    }

    @Test
    void testDeleteNamespacePermissionsOfUser_ShouldThrowException_WhenRepositoryFails() {
        String namespaceId = "1000001";
        String username = "testuser";
        AuthenticateResponseDto mockAuthDto = mock(AuthenticateResponseDto.class);
        doReturn(true).when(namespaceService).isAbleToEdit(Integer.parseInt(namespaceId), mockAuthDto);
        lenient().doThrow(new RuntimeException("DB error")).when(permissionsNamespaceRepository).deleteByNamespaceAndUsername(Integer.parseInt(namespaceId), username);
        assertThrows(RuntimeException.class, () -> namespaceService.deleteNamespacePermissionsOfUser(namespaceId, username, mockAuthDto));
    }

    @Test
    void testGetNamespacePermissions_WithResults() throws CisException {
        String namespaceId = "1001";
        PermissionsNamespace permission = new PermissionsNamespace();
        permission.setNamespace(1001);
        List<PermissionsNamespace> mockList = List.of(permission);
        when(permissionsNamespaceRepository.findByNamespace(1001)).thenReturn(mockList);
        List<PermissionsNamespace> result = namespaceService.getNamespacePermissions(namespaceId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals((Integer) 1001, result.get(0).getNamespace());
        verify(permissionsNamespaceRepository).findByNamespace(1001);
    }

    @Test
    void testGetNamespacePermissions_EmptyResult() throws CisException {
        String namespaceId = "2002";
        when(permissionsNamespaceRepository.findByNamespace(2002)).thenReturn(Collections.emptyList());
        List<PermissionsNamespace> result = namespaceService.getNamespacePermissions(namespaceId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(permissionsNamespaceRepository).findByNamespace(2002);
    }

    @Test
    void testGetNamespacePermissions_RepositoryThrowsException() {
        String namespaceId = "3003";
        when(permissionsNamespaceRepository.findByNamespace(3003)).thenThrow(new RuntimeException("DB error"));
        CisException exception = assertThrows(CisException.class, () -> {
            namespaceService.getNamespacePermissions(namespaceId);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("DB error"));
        verify(permissionsNamespaceRepository).findByNamespace(3003);
    }

    @Test
    void testDeleteNamespacePermissionsOfUser_ShouldDeleteSuccessfully() throws CisException {
        String namespaceId = "1001";
        String username = "john_doe";
        AuthenticateResponseDto authenticateResponseDto = mock(AuthenticateResponseDto.class);
        doReturn(true).when(namespaceService).isAbleToEdit(1001, authenticateResponseDto);
        String result = namespaceService.deleteNamespacePermissionsOfUser(namespaceId, username, authenticateResponseDto);
        assertTrue(result.contains("Success"));
        verify(namespaceService).isAbleToEdit(1001, authenticateResponseDto);
        verify(permissionsNamespaceRepository).deleteByNamespaceAndUsername(1001, username);
    }

    @Test
    void testDeleteNamespacePermissionsOfUser_ShouldThrowException_WhenNamespaceIdIsInvalid() {
        String invalidNamespaceId = "abc123";
        String username = "john_doe";
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        assertThrows(NumberFormatException.class, () -> namespaceService.deleteNamespacePermissionsOfUser(invalidNamespaceId, username, mockDto));
    }

    @Test
    void testCreateNamespacePermissionsOfUser_Success() throws CisException {
        String namespaceId = "1001";
        String username = "john_doe";
        String role = "admin";
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        when(namespaceService.isAbleToEdit(1001, mockDto)).thenReturn(true);
        when(permissionsNamespaceRepository.findByNamespaceAndUsernameAndRole(1001, username, role)).thenReturn(Optional.empty());
        String result = namespaceService.createNamespacePermissionsOfUser(namespaceId, username, role, mockDto);
        assertTrue(result.contains("Success"));
        verify(permissionsNamespaceRepository).save(any(PermissionsNamespace.class));
    }

    @Test
    void testCreateNamespacePermissionsOfUser_DuplicateEntry()  {
        String namespaceId = "1001";
        String username = "john_doe";
        String role = "admin";
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        when(namespaceService.isAbleToEdit(1001, mockDto)).thenReturn(true);
        PermissionsNamespace existing = new PermissionsNamespace(1001, username, role);
        when(permissionsNamespaceRepository.findByNamespaceAndUsernameAndRole(1001, username, role)).thenReturn(Optional.of(existing));
        CisException ex = assertThrows(CisException.class, () -> namespaceService.createNamespacePermissionsOfUser(namespaceId, username, role, mockDto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("ER_DUP_ENTRY"));
    }

    @Test
    void testCreateNamespacePermissionsOfUser_Unauthorized()  {
        String namespaceId = "1001";
        String username = "john_doe";
        String role = "admin";
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        when(namespaceService.isAbleToEdit(1001, mockDto)).thenReturn(false);
        CisException ex = assertThrows(CisException.class, () -> namespaceService.createNamespacePermissionsOfUser(namespaceId, username, role, mockDto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("No permission"));
    }

    @Test
    void testCreateNamespacePermissionsOfUser_InvalidNamespaceId() {
        String invalidNamespaceId = "abc";
        String username = "john_doe";
        String role = "admin";
        AuthenticateResponseDto mockDto = mock(AuthenticateResponseDto.class);
        assertThrows(NumberFormatException.class, () -> namespaceService.createNamespacePermissionsOfUser(invalidNamespaceId, username, role, mockDto));
    }

}

