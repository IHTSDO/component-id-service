package org.snomed.cis.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.util.SctIdHelper;
import org.snomed.cis.util.StateMachine;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchemeIdServiceTest {

    @InjectMocks
    private SchemeIdService schemeIdService;

    @Mock
    private AuthenticateResponseDto authToken;

    @Mock
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Mock
    private BulkSchemeIdRepository bulkSchemeIdRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query mockQuery;

    @Mock
    private SctIdHelper sctIdHelper;

    @Mock
    private StateMachine stateMachine;

    private void injectStateMachine(String action, String statusValue) {
        stateMachine = new StateMachine();
        stateMachine.actions = new HashMap<>();
        stateMachine.actions.put(action, statusValue);
        ReflectionTestUtils.setField(schemeIdService, "stateMachine", stateMachine);
    }


    @Test
    void isAbleUser_WithAdminRole_ReturnsTrue() throws CisException {
        String schemeName = "SNOMEDID";
        List<String> roles = List.of("ROLE_component-identifier-service-admin");

        when(authToken.getRoles()).thenReturn(roles);

        boolean result = schemeIdService.isAbleUser(schemeName, authToken);
        Assertions.assertTrue(result);
    }

    @Test
    void isAbleUser_WithSchemePermission_ReturnsTrue() throws CisException {
        String schemeName = "SNOMEDID";
        List<String> roles = List.of("ROLE_not-admin");

        when(authToken.getRoles()).thenReturn(roles);
        SchemeIdService spyService = Mockito.spy(schemeIdService);
        doReturn(true).when(spyService).hasSchemePermission(schemeName, authToken);

        boolean result = spyService.isAbleUser(schemeName, authToken);
        Assertions.assertTrue(result);
    }

    @Test
    void isAbleUser_WithoutAdminOrSchemePermission_ReturnsFalse() throws CisException {
        String schemeName = "SNOMEDID";
        List<String> roles = List.of("ROLE_not-admin");

        when(authToken.getRoles()).thenReturn(roles);
        SchemeIdService spyService = Mockito.spy(schemeIdService);
        doReturn(false).when(spyService).hasSchemePermission(schemeName, authToken);

        boolean result = spyService.isAbleUser(schemeName, authToken);
        Assertions.assertFalse(result);
    }


    @Test
    void hasSchemePermission_UserHasDirectPermission_ReturnsTrue() throws CisException {
        String schemeName = "SNOMEDID";
        String username = "test-user";

        when(authToken.getName()).thenReturn(username);

        PermissionsScheme perm = new PermissionsScheme();
        perm.setScheme(schemeName);
        perm.setRole("user");
        perm.setUsername(username);

        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(List.of(perm));

        boolean result = schemeIdService.hasSchemePermission(schemeName, authToken);

        Assertions.assertTrue(result);
    }


    @Test
    void hasSchemePermission_UserHasGroupPermission_ReturnsTrue() throws CisException {
        String schemeName = "SNOMEDID";
        String groupName = "test-group";

        when(authToken.getRoles()).thenReturn(List.of("ROLE_" + groupName));

        PermissionsScheme perm = new PermissionsScheme();
        perm.setScheme(schemeName);
        perm.setRole("group");
        perm.setUsername(groupName);

        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(List.of(perm));

        boolean result = schemeIdService.hasSchemePermission(schemeName, authToken);

        Assertions.assertTrue(result);
    }

    @Test
    void hasSchemePermission_UserHasNoPermission_ReturnsFalse() throws CisException {
        String schemeName = "SNOMEDID";

        when(authToken.getRoles()).thenReturn(List.of("ROLE_random-group"));

        PermissionsScheme perm = new PermissionsScheme();
        perm.setRole("group");
        perm.setUsername("some-other-group");

        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(List.of(perm));

        boolean result = schemeIdService.hasSchemePermission(schemeName, authToken);

        Assertions.assertFalse(result);
    }


    @Test
    void hasSchemePermission_RoleParsingThrowsException_ThrowsCisException() {
        String schemeName = "SNOMEDID";

        when(authToken.getRoles()).thenThrow(new RuntimeException("role error"));

        PermissionsScheme perm = new PermissionsScheme();
        perm.setRole("group");
        perm.setUsername("test-group");

        when(permissionsSchemeRepository.findByScheme(schemeName)).thenReturn(List.of(perm));

        CisException ex = assertThrows(CisException.class,
                () -> schemeIdService.hasSchemePermission(schemeName, authToken));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        Assertions.assertEquals("Error while fetching groups from authToken.", ex.getMessage());
    }

    @Test
    void getSchemeIds_ShouldCallGetSchemeIdsList_AndReturnResult() throws Exception {
        String limit = "10";
        String skip = "5";

        List<SchemeId> expectedList = List.of(new SchemeId(), new SchemeId());

        SchemeIdService spyService = Mockito.spy(schemeIdService);
        doReturn(expectedList).when(spyService).getSchemeIdsList(limit, skip, SchemeName.SNOMEDID, authToken);

        List<SchemeId> result = spyService.getSchemeIds(authToken, limit, skip, SchemeName.SNOMEDID);
        assertEquals(expectedList, result);
        verify(spyService, times(1)).getSchemeIdsList(limit, skip, SchemeName.SNOMEDID, authToken);
    }

    private List<SchemeId> generateMockSchemeIds(int count) {
        List<SchemeId> resultList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            SchemeId schemeId = new SchemeId();
            schemeId.setScheme(SchemeName.SNOMEDID.toString());
            schemeId.setSchemeId(String.valueOf(i));
            resultList.add(schemeId);
        }
        return resultList;
    }

    @Test
    void testGetSchemeIdsList_withLimitAndSkip() throws Exception {

        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        List<SchemeId> mockSchemeIds = generateMockSchemeIds(5);

        when(entityManager.createNativeQuery(anyString(), eq(SchemeId.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockSchemeIds);

        List<SchemeId> result = schemeIdService.getSchemeIdsList("2", "2", SchemeName.SNOMEDID, authToken);

        assertEquals(2, result.size());
        assertEquals("3", result.get(0).getSchemeId());
    }

    @Test
    void testGetSchemeIdsList_unauthorizedUser_shouldThrowException() {
        when(authToken.getRoles()).thenReturn(List.of("ROLE_not-admin"));

        CisException exception = assertThrows(
                CisException.class,
                () -> schemeIdService.getSchemeIdsList("2", "0", SchemeName.SNOMEDID, authToken)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        Assertions.assertTrue(exception.getMessage().contains("No permission"));
    }

    @Test
    void testGetSchemeIdsList_returnsEmptyList() throws Exception {
        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        when(entityManager.createNativeQuery(anyString(), eq(SchemeId.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(Collections.emptyList());

        List<SchemeId> result = schemeIdService.getSchemeIdsList("10", "0", SchemeName.SNOMEDID, authToken);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testGetSchemeIdsList_withInvalidLimit_shouldThrowNumberFormatException() {
        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));

        assertThrows(NumberFormatException.class, () ->
                schemeIdService.getSchemeIdsList("abc", "0", SchemeName.SNOMEDID, authToken)
        );
    }

    @Test
    void testGetSchemeId_shouldReturnSchemeId() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;
        String schemeIdValue = "0-00001";

        SchemeId expected = new SchemeId();
        expected.setScheme(schemeName.toString());
        expected.setSchemeId(schemeIdValue);

        SchemeIdService spyService = Mockito.spy(schemeIdService);
        doReturn(expected).when(spyService).getSchemeIdsByschemeIdList(schemeName.toString(), schemeIdValue);

        SchemeId result = spyService.getSchemeId(authToken, schemeName, schemeIdValue);

        assertNotNull(result);
        assertEquals(expected.getSchemeId(), result.getSchemeId());
        assertEquals(expected.getScheme(), result.getScheme());
    }

    @Test
    void testGetSchemeIdsByschemeIdList_validInput_shouldReturnRecordFromDB() throws Exception {
        String schemeIdValue = "0-00001";
        String schemeName = "SNOMEDID";

        SchemeId expected = new SchemeId();
        expected.setScheme(schemeName);
        expected.setSchemeId(schemeIdValue);

        when(bulkSchemeIdRepository.findBySchemeAndSchemeId(schemeName, schemeIdValue))
                .thenReturn(Optional.of(expected));

        SchemeId result = schemeIdService.getSchemeIdsByschemeIdList(schemeName, schemeIdValue);

        assertNotNull(result);
        assertEquals(expected.getSchemeId(), result.getSchemeId());
        assertEquals(expected.getScheme(), result.getScheme());
    }

    @Test
    void testGetSchemeIdsByschemeIdList_shouldThrowException_whenInvalidSchemeId() {
        String schemeName = "SNOMEDID";
        String schemeIdValue = "invalid-id";

        CisException exception = assertThrows(CisException.class, () -> {
            schemeIdService.getSchemeIdsByschemeIdList(schemeName, schemeIdValue);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Not valid schemeId.", exception.getMessage());
    }

    @Test
    void testGetNewRecord_shouldReturnCorrectMap() {
        String schemeName = "SNOMEDID";
        String schemeId = "100-001";

        Map<String, Object> result = schemeIdService.getNewRecord(schemeName, schemeId);

        assertEquals(schemeName, result.get("scheme"));
        assertEquals(schemeId, result.get("schemeId"));
        Assertions.assertNull(result.get("sequence"));
        Assertions.assertNull(result.get("checkDigit"));
        assertNotNull(result.get("systemId"));
        assertEquals(5, result.size());
    }

    @Test
    void testInsertSchemeIdRecord_shouldSaveAndReturnSchemeId() throws Exception {
        Map<String, Object> schemeIdRecord = new HashMap<>();
        schemeIdRecord.put("scheme", "SNOMEDID");
        schemeIdRecord.put("schemeId", "0-12345");
        schemeIdRecord.put("sequence", 1);
        schemeIdRecord.put("checkDigit", 2);
        schemeIdRecord.put("systemId", "test-system-id");
        schemeIdRecord.put("status", "active");
        schemeIdRecord.put("author", "test-author");
        schemeIdRecord.put("software", "test-sw");
        schemeIdRecord.put("expirationDate", LocalDateTime.of(2025, 1, 1, 0, 0));
        schemeIdRecord.put("jobId", 100);
        schemeIdRecord.put("created_at", LocalDateTime.of(2024, 12, 1, 0, 0));
        schemeIdRecord.put("modified_at", LocalDateTime.of(2024, 12, 15, 0, 0));

        SchemeId expected = SchemeId.builder()
                .scheme("SNOMEDID")
                .schemeId("0-12345")
                .sequence(1)
                .checkDigit(2)
                .systemId("test-system-id")
                .status("active")
                .author("test-author")
                .software("test-sw")
                .expirationDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .jobId(100)
                .created_at(LocalDateTime.of(2024, 12, 1, 0, 0))
                .modified_at(LocalDateTime.of(2024, 12, 15, 0, 0))
                .build();

        when(bulkSchemeIdRepository.save(any(SchemeId.class))).thenReturn(expected);

        SchemeId result = schemeIdService.insertSchemeIdRecord(schemeIdRecord);

        assertNotNull(result);
        assertEquals("0-12345", result.getSchemeId());
        assertEquals("SNOMEDID", result.getScheme());
        assertEquals("test-system-id", result.getSystemId());
        assertEquals(1, result.getSequence());
        assertEquals(2, result.getCheckDigit());
        assertEquals("active", result.getStatus());
    }

    @Test
    void testGetSchemeIdsBysystemList_shouldReturnSchemeId() throws CisException {
        String scheme = "SNOMEDID";
        String systemId = "123";

        SchemeId expectedSchemeId = new SchemeId();
        expectedSchemeId.setSystemId(systemId);

        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        when(bulkSchemeIdRepository.findBySchemeAndSystemId(scheme, systemId)).thenReturn(List.of(expectedSchemeId));

        SchemeId result = schemeIdService.getSchemeIdsBysystemList(scheme, systemId, authToken);

        assertEquals(expectedSchemeId, result);
    }

    @Test
    void testGetSchemeIdsBysystemList_whenUnauthorized_thenThrowsException() {
        String scheme = "SNOMEDID";
        String systemId = "123";

        when(authToken.getRoles()).thenReturn(List.of("ROLE_not-admin"));

        CisException exception = assertThrows(CisException.class, () -> {
            schemeIdService.getSchemeIdsBysystemList(scheme, systemId, authToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        Assertions.assertTrue(exception.getMessage().contains("No permission"));
    }

    @Test
    void testGetSchemeIdsBysystemList_noSchemeId_shouldThrowCisException() {
        String scheme = "SNOMEDID";
        String systemId = "non-existent-system-id";

        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        when(bulkSchemeIdRepository.findBySchemeAndSystemId(scheme, systemId)).thenReturn(Collections.emptyList());

        CisException thrown = assertThrows(CisException.class, () -> {
            schemeIdService.getSchemeIdsBysystemList(scheme, systemId, authToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatus());
        Assertions.assertTrue(thrown.getMessage().contains("No schemeid for scheme"));
    }

    // deprecateSchemeIds tests - start

    @Test
    void testDeprecateSchemeIds_shouldDelegateAndReturn() throws CisException {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdUpdateRequestDto request = new SchemeIdUpdateRequestDto();
        SchemeId expected = new SchemeId();

        SchemeIdService spyService = Mockito.spy(schemeIdService);
        doReturn(expected).when(spyService).deprecateSchemeIdList(schemeName, request, authToken);

        SchemeId result = spyService.deprecateSchemeIds(authToken, schemeName, request);

        assertEquals(expected, result);
        verify(spyService).deprecateSchemeIdList(schemeName, request, authToken);
    }


    @Test
    void testDeprecateSchemeIdList_SuccessfulDeprecation() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-0000A");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Deprecated for testing");

        authToken.setName("testUser");

        SchemeId existingSchemeId = new SchemeId();
        existingSchemeId.setSchemeId("0-00001");
        existingSchemeId.setStatus("Published");

        SchemeId updatedSchemeId = new SchemeId();
        updatedSchemeId.setSchemeId("0-00002");
        updatedSchemeId.setStatus("Published");
        updatedSchemeId.setAuthor("testUser");
        updatedSchemeId.setSoftware("TestSoftware");
        updatedSchemeId.setComment("Deprecated for testing");

        // Mocks
        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        injectStateMachine("deprecate", "Deprecate");
        when(schemeIdService.getSchemeIdsByschemeIdList(schemeName.toString(), "0-00001")).thenReturn(existingSchemeId);


        // Use doReturn() to avoid argument mismatch errors
        doReturn(updatedSchemeId).when(bulkSchemeIdRepository).save(any(SchemeId.class));

        // Act
        SchemeId result = schemeIdService.deprecateSchemeIdList(schemeName, requestDto, authToken);

        // Assert
        assertNotNull(result);
        assertEquals("0-00002", result.getSchemeId());
        assertEquals("Deprecated", result.getStatus());
        assertEquals("TestSoftware", result.getSoftware());
        assertEquals("Deprecated for testing", result.getComment());
    }

    @Test
    void testDeprecateSchemeIdList_UnauthorizedUser() {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-00001");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Testing unauthorized case");


        when(authToken.getRoles()).thenReturn(List.of("ROLE_not-admin"));
        CisException ex = assertThrows(CisException.class, () ->
                schemeIdService.deprecateSchemeIdList(schemeName, requestDto, authToken)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        Assertions.assertTrue(ex.getMessage().contains("No permission"));
    }

    @Test
    void testDeprecateSchemeIdList_InvalidStateTransition() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-00001");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Deprecated for testing");

        authToken.setName("testUser");
        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));

        SchemeId existingSchemeId = new SchemeId();
        existingSchemeId.setSchemeId("0-00001");
        existingSchemeId.setStatus("Assigned");

        SchemeIdService schemeIdServiceSpy = Mockito.spy(schemeIdService);
        StateMachine mockedStateMachine = Mockito.mock(StateMachine.class);

        Map<String, String> actions = new HashMap<>();
        actions.put("deprecate", "Deprecate");
        mockedStateMachine.actions = actions;
        ReflectionTestUtils.setField(schemeIdServiceSpy, "stateMachine", mockedStateMachine);

        doReturn(existingSchemeId).when(schemeIdServiceSpy).getSchemeIdsByschemeIdList("SNOMEDID", "0-00001");
        when(schemeIdServiceSpy.isAbleUser("SNOMEDID", authToken)).thenReturn(true);

        when(mockedStateMachine.getNewStatus("Assigned", "Deprecate")).thenReturn(null);

        CisException thrown = assertThrows(CisException.class, () ->
                schemeIdServiceSpy.deprecateSchemeIdList(schemeName, requestDto, authToken)
        );

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        Assertions.assertTrue(thrown.getMessage().contains("Cannot deprecate SchemeId"));
    }

    // deprecateSchemeIds tests - end

    // releaseSchemeIds tests - start
    @Test
    void testReleaseSchemeIdList_SuccessfulRelease() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-00001");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Releasing for testing");

        authToken.setName("testUser");

        SchemeId existingSchemeId = new SchemeId();
        existingSchemeId.setSchemeId("0-00001");
        existingSchemeId.setStatus("Published");

        SchemeId updatedSchemeId = new SchemeId();
        updatedSchemeId.setSchemeId("0-00001");
        updatedSchemeId.setStatus("Assigned");
        updatedSchemeId.setAuthor("testUser");
        updatedSchemeId.setSoftware("TestSoftware");
        updatedSchemeId.setComment("Releasing for testing");

        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));

        injectStateMachine("release", "Release");

        when(schemeIdService.getSchemeIdsByschemeIdList(schemeName.toString(), "0-00001")).thenReturn(existingSchemeId);

        doReturn(updatedSchemeId).when(bulkSchemeIdRepository).save(any(SchemeId.class));

        SchemeId result = schemeIdService.releaseSchemeIdList(schemeName, requestDto, authToken);

        assertNotNull(result);
        assertEquals("0-00001", result.getSchemeId());
        assertEquals("Available", result.getStatus());
        assertEquals("TestSoftware", result.getSoftware());
        assertEquals("Releasing for testing", result.getComment());
    }


    @Test
    void testReleaseSchemeIdList_UnauthorizedUser() {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-00001");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Testing unauthorized access");

        when(authToken.getRoles()).thenReturn(List.of("ROLE_not-admin"));

        CisException ex = assertThrows(CisException.class, () ->
                schemeIdService.releaseSchemeIdList(schemeName, requestDto, authToken)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("No permission"));
    }

    @Test
    void testReleaseSchemeIdList_InvalidStateTransition() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-00001");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Testing invalid state transition");

        authToken.setName("testUser");
        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));

        SchemeId existingSchemeId = new SchemeId();
        existingSchemeId.setSchemeId("0-00001");
        existingSchemeId.setStatus("Assigned");

        SchemeIdService schemeIdServiceSpy = Mockito.spy(schemeIdService);

        StateMachine mockedStateMachine = Mockito.mock(StateMachine.class);
        Map<String, String> actions = new HashMap<>();
        actions.put("release", "Release");
        mockedStateMachine.actions = actions;
        ReflectionTestUtils.setField(schemeIdServiceSpy, "stateMachine", mockedStateMachine);

        // Mock service calls
        doReturn(existingSchemeId).when(schemeIdServiceSpy).getSchemeIdsByschemeIdList("SNOMEDID", "0-00001");
        when(schemeIdServiceSpy.isAbleUser("SNOMEDID", authToken)).thenReturn(true);

        when(mockedStateMachine.getNewStatus("Assigned", "Release")).thenReturn(null);

        CisException thrown = assertThrows(CisException.class, () ->
                schemeIdServiceSpy.releaseSchemeIdList(schemeName, requestDto, authToken)
        );

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Cannot release SchemeId"));
    }

    // releaseSchemeIds tests - end

    // publishSchemeIds tests - start

    @Test
    void testPublishSchemeIdList_SuccessfulPublish() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-0000A");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Publishing for testing");

        authToken.setName("testUser");

        SchemeId existingSchemeId = new SchemeId();
        existingSchemeId.setSchemeId("0-00001");
        existingSchemeId.setStatus("assigned");

        SchemeId updatedSchemeId = new SchemeId();
        updatedSchemeId.setSchemeId("0-00001");
        updatedSchemeId.setStatus("assigned");
        updatedSchemeId.setAuthor("testUser");
        updatedSchemeId.setSoftware("TestSoftware");
        updatedSchemeId.setComment("Publishing for testing");

        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        injectStateMachine("publish", "Publish");
        when(schemeIdService.getSchemeIdsByschemeIdList(schemeName.toString(), "0-0000A")).thenReturn(existingSchemeId);

        doReturn(updatedSchemeId).when(bulkSchemeIdRepository).save(any(SchemeId.class));

        SchemeId result = schemeIdService.publishSchemeIdList(schemeName, requestDto, authToken);

        assertNotNull(result);
        assertEquals("0-00001", result.getSchemeId());
        assertEquals("Published", result.getStatus());
        assertEquals("TestSoftware", result.getSoftware());
        assertEquals("Publishing for testing", result.getComment());
    }

    @Test
    void testPublishSchemeIdList_UnauthorizedUser() {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-0000A");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Trying to publish without permission");

        when(authToken.getRoles()).thenReturn(List.of("ROLE_not-admin"));

        CisException ex = assertThrows(CisException.class, () ->
                schemeIdService.publishSchemeIdList(schemeName, requestDto, authToken)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("No permission"));
    }


    @Test
    void testPublishSchemeIdList_InvalidStateTransition() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;

        SchemeIdUpdateRequestDto requestDto = new SchemeIdUpdateRequestDto();
        requestDto.setSchemeId("0-00001");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Testing invalid state transition");

        authToken.setName("testUser");
        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));

        SchemeId existingSchemeId = new SchemeId();
        existingSchemeId.setSchemeId("0-00001");
        existingSchemeId.setStatus("Assigned");

        SchemeIdService schemeIdServiceSpy = Mockito.spy(schemeIdService);

        StateMachine mockedStateMachine = Mockito.mock(StateMachine.class);
        Map<String, String> actions = new HashMap<>();
        actions.put("publish", "Publish");
        mockedStateMachine.actions = actions;
        ReflectionTestUtils.setField(schemeIdServiceSpy, "stateMachine", mockedStateMachine);

        doReturn(existingSchemeId).when(schemeIdServiceSpy).getSchemeIdsByschemeIdList("SNOMEDID", "0-00001");
        when(schemeIdServiceSpy.isAbleUser("SNOMEDID", authToken)).thenReturn(true);
        when(mockedStateMachine.getNewStatus("Assigned", "Publish")).thenReturn(null);

        CisException thrown = assertThrows(CisException.class, () ->
                schemeIdServiceSpy.publishSchemeIdList(schemeName, requestDto, authToken)
        );

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatus());
        assertTrue(thrown.getMessage().contains("Cannot publish SchemeId"));
    }

    // publishSchemeIds tests - end

    // reserveSchemeIds tests - start

    @Test
    void shouldReserveSchemeId_whenUserHasPermission() throws Exception {
        injectStateMachine("reserve", "RESERVED");

        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdReserveRequestDto requestDto = new SchemeIdReserveRequestDto();
        requestDto.setSoftware("TestSoftware");
        requestDto.setExpirationDate(String.valueOf(LocalDate.now().plusDays(30)));
        requestDto.setComment("Test comment");

        authToken.setName("testUser");

        SchemeId expectedSchemeId = new SchemeId();

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(true).when(spyService).isAbleUser(schemeName.toString(), authToken);
        doReturn(expectedSchemeId).when(spyService)
                .setNewSchemeIdRecord(eq(schemeName), any(SchemeIdReserveRequest.class), eq("RESERVED"));

        SchemeId actualSchemeId = spyService.reserveSchemeId(authToken, schemeName, requestDto);

        assertNotNull(actualSchemeId);
        assertEquals(expectedSchemeId, actualSchemeId);
    }

    @Test
    void shouldThrowUnauthorized_whenUserHasNoPermission() throws CisException {

        injectStateMachine("reserve", "RESERVED");

        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdReserveRequestDto requestDto = new SchemeIdReserveRequestDto();
        requestDto.setSoftware("TestSoftware");

        authToken.setName("unauthorizedUser");

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(false).when(spyService).isAbleUser(schemeName.toString(), authToken);

        CisException exception = assertThrows(CisException.class, () ->
                spyService.reserveSchemeId(authToken, schemeName, requestDto)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("No permission for the selected operation", exception.getMessage());
    }

    // reserveSchemeIds tests - end

    // generateSchemeId tests - start

    @Test
    void testGenerateSchemeId_withAutoGeneratedSystemId_success() throws Exception {
        injectStateMachine("generate", "assigned");

        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdGenerateRequestDto requestDto = new SchemeIdGenerateRequestDto();
        requestDto.setSystemId(null);
        requestDto.setSoftware("AutoSoftware");
        requestDto.setComment("Auto gen test");

        authToken.setName("autoUser");

        SchemeId expectedSchemeId = new SchemeId();
        expectedSchemeId.setSchemeId("0-00001");

        try (MockedStatic<SctIdHelper> mockedHelper = Mockito.mockStatic(SctIdHelper.class)) {
            mockedHelper.when(SctIdHelper::guid).thenReturn("AUTO-GUID");

            SchemeIdService spyService = spy(schemeIdService);
            doReturn(true).when(spyService).isAbleUser(schemeName.toString(), authToken);
            doReturn(expectedSchemeId).when(spyService)
                    .setNewSchemeIdRecordGen(eq(schemeName.toString()), any(SchemeIdGenerateRequest.class), eq("assigned"));

            SchemeId result = spyService.generateSchemeId(authToken, schemeName, requestDto);

            assertNotNull(result);
            assertEquals("0-00001", result.getSchemeId());
        }
    }


    @Test
    void testGenerateSchemeId_withProvidedSystemId_success() throws Exception {
        injectStateMachine("generate", "assigned");

        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdGenerateRequestDto requestDto = new SchemeIdGenerateRequestDto();
        requestDto.setSystemId("SYS123");
        requestDto.setSoftware("ManualSoftware");
        requestDto.setComment("Manual gen test");

        authToken.setName("manualUser");

        SchemeId expectedSchemeId = new SchemeId();
        expectedSchemeId.setSchemeId("0-00001");

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(true).when(spyService).isAbleUser(schemeName.toString(), authToken);

        doReturn(null).when(spyService).getSchemeIdBySystemId(schemeName.toString(), "SYS123");

        doReturn(expectedSchemeId).when(spyService)
                .setNewSchemeIdRecordGen(eq(schemeName.toString()), any(SchemeIdGenerateRequest.class), eq("assigned"));

        SchemeId result = spyService.generateSchemeId(authToken, schemeName, requestDto);

        assertNotNull(result);
        assertEquals("0-00001", result.getSchemeId());
    }

    @Test
    void testGenerateSchemeId_existingSystemId_returnsExistingSchemeId() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdGenerateRequestDto requestDto = new SchemeIdGenerateRequestDto();
        requestDto.setSystemId("DUPLICATE-SYS");  // Provided systemId
        requestDto.setSoftware("DupSoftware");
        requestDto.setComment("Duplicate system ID test");

        authToken.setName("existingUser");

        SchemeId existingSchemeId = new SchemeId();
        existingSchemeId.setSchemeId("0-00001");

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(true).when(spyService).isAbleUser(schemeName.toString(), authToken);

        doReturn(existingSchemeId).when(spyService).getSchemeIdBySystemId(schemeName.toString(), "DUPLICATE-SYS");

        SchemeId result = spyService.generateSchemeId(authToken, schemeName, requestDto);

        assertNotNull(result);
        assertEquals("0-00001", result.getSchemeId());
    }

    @Test
    void testGenerateSchemeId_unauthorizedUser_throwsException() throws CisException {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdGenerateRequestDto requestDto = new SchemeIdGenerateRequestDto();
        requestDto.setSystemId("SYS123");
        requestDto.setSoftware("NoAccessSoft");

        authToken.setName("unauthUser");

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(false).when(spyService).isAbleUser(schemeName.toString(), authToken);

        CisException ex = assertThrows(CisException.class, () ->
                spyService.generateSchemeId(authToken, schemeName, requestDto)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getMessage());
    }

    // generateSchemeId tests - end

    // registerSchemeId tests - start

    @Test
    void testRegisterSchemeId_withGeneratedSystemId_success() throws Exception {
        injectStateMachine("register", "REGISTERED");

        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdRegisterRequestDto requestDto = new SchemeIdRegisterRequestDto();
        requestDto.setSchemeId("0-0000A");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Test registration with auto-generated SystemId");

        authToken.setName("testUser");

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(true).when(spyService).isAbleUser(schemeName.toString(), authToken);

        try (MockedStatic<SctIdHelper> mockedStatic = Mockito.mockStatic(SctIdHelper.class)) {
            mockedStatic.when(SctIdHelper::guid).thenReturn("auto-generated-system-id");

            SchemeId expectedSchemeId = new SchemeId();
            expectedSchemeId.setSchemeId("0-0000A");
            expectedSchemeId.setSystemId("auto-generated-system-id");

            doReturn(expectedSchemeId).when(spyService).registerNewSchemeId(eq(schemeName), any(SchemeIdRegisterRequest.class));

            SchemeId result = spyService.registerSchemeId(authToken, schemeName, requestDto);

            assertNotNull(result);
            assertEquals("0-0000A", result.getSchemeId());
            assertEquals("auto-generated-system-id", result.getSystemId());
        }
    }


    @Test
    void testRegisterSchemeId_unauthorizedUser_throwsException() throws Exception {
        injectStateMachine("register", "REGISTERED");

        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdRegisterRequestDto requestDto = new SchemeIdRegisterRequestDto();
        requestDto.setSchemeId("0-0000C");
        requestDto.setSystemId("SYS123");
        requestDto.setSoftware("UnauthorizedSoftware");
        requestDto.setComment("Test registration with unauthorized user");

        authToken.setName("unauthorizedUser");

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(false).when(spyService).isAbleUser(schemeName.toString(), authToken);

        CisException ex = assertThrows(CisException.class, () ->
                spyService.registerSchemeId(authToken, schemeName, requestDto)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getMessage());
    }


    @Test
    void testRegisterSchemeId_duplicateSystemId_throwsException() throws Exception {
        injectStateMachine("register", "REGISTERED");

        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdRegisterRequestDto requestDto = new SchemeIdRegisterRequestDto();
        requestDto.setSchemeId("0-0000C");
        requestDto.setSystemId("SYS123");
        requestDto.setSoftware("TestSoftware");
        requestDto.setComment("Test registration with duplicate SystemId");

        authToken.setName("testUser");

        SchemeId existingSchemeId = new SchemeId();
        existingSchemeId.setSchemeId("0-0000A");
        existingSchemeId.setSystemId("SYS123");


        SchemeIdService spyService = spy(schemeIdService);
        doReturn(true).when(spyService).isAbleUser(schemeName.toString(), authToken);


        doReturn(existingSchemeId).when(spyService).getSchemeIdBySystemId(schemeName.toString(), "SYS123");


        CisException ex = assertThrows(CisException.class, () ->
                spyService.registerSchemeId(authToken, schemeName, requestDto)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("SystemIdSYS123 already exists with SchemeId:0-0000A", ex.getMessage());
    }

    // registerSchemeId tests - end

    // test free records

    @Test
    void testGetFreeRecords_success() throws Exception {
        String schemeName = "SNOMEDID", schemeId = "0-0000A";
        Map<String, Object> mockRecord = new HashMap<>();
        mockRecord.put("schemeId", schemeId);
        SchemeId expectedSchemeId = new SchemeId();
        expectedSchemeId.setSchemeId(schemeId);
        expectedSchemeId.setStatus("Available");

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(mockRecord).when(spyService).getNewRecord(schemeName, schemeId);
        doReturn(expectedSchemeId).when(spyService).insertSchemeIdRecord(any(Map.class));

        SchemeId result = spyService.getFreeRecords(schemeName, schemeId);

        assertEquals("Available", result.getStatus());
    }


    @Test
    void testSetAvailableSchemeIdRecord2NewStatusGen_withAvailableStatus_updatesSchemeId() throws Exception {
        String schemeName = "SNOMEDID";
        SchemeIdGenerateRequest request = new SchemeIdGenerateRequest();
        request.setSystemId("SYS123");
        request.setAuthor("author");
        request.setSoftware("software");
        request.setComment("Updated record");

        List<SchemeId> schemeIdRecords = new ArrayList<>();
        SchemeId schemeId = new SchemeId();
        schemeId.setStatus("Available");
        schemeId.setSchemeId("0-0000A");
        schemeIdRecords.add(schemeId);

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(schemeIdRecords).when(spyService).findschemeRecord(any(), anyString(), any());
        doReturn("newStatus").when(stateMachine).getNewStatus("Available", "generate");

        doReturn(schemeId).when(bulkSchemeIdRepository).save(any(SchemeId.class));

        SchemeId result = spyService.setAvailableSchemeIdRecord2NewStatusGen(schemeName, request, "generate");

        assertNotNull(result);
        assertEquals("SYS123", result.getSystemId());
        assertEquals("newStatus", result.getStatus());
        assertEquals("author", result.getAuthor());
        assertEquals("software", result.getSoftware());
        assertEquals("Updated record", result.getComment());
    }


    @Test
    void testSetAvailableSchemeIdRecord2NewStatusGen_noAvailableSchemeId_callsCounterModeGen() throws Exception {
        String schemeName = "SNOMEDID";
        SchemeIdGenerateRequest request = new SchemeIdGenerateRequest();
        request.setSystemId("SYS123");
        request.setAuthor("author");
        request.setSoftware("software");
        request.setComment("New record");

        List<SchemeId> schemeIdRecords = new ArrayList<>();

        SchemeIdService spyService = spy(schemeIdService);
        doReturn(schemeIdRecords).when(spyService).findschemeRecord(any(), anyString(), any());

        doReturn(new SchemeId()).when(spyService).counterModeGen(schemeName, request, "generate");

        SchemeId result = spyService.setAvailableSchemeIdRecord2NewStatusGen(schemeName, request, "generate");

        assertNotNull(result);
        verify(spyService, times(1)).counterModeGen(schemeName, request, "generate");
    }

}
