package org.snomed.cis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.controller.SecurityController;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkJobRepository;
import org.snomed.cis.repository.PermissionsNamespaceRepository;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.util.JobTypeConstants;
import org.snomed.cis.util.ModelsConstants;
import org.snomed.cis.util.SctIdHelper;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;


import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class BulkSctidServiceTest {
    @Spy
    @InjectMocks
    private BulkSctidService bulkSctidService;
    @Mock
    private AuthenticateResponseDto authenticateResponseDto;
    @Mock
    private SctidRepository repo;
    @Mock
    private JobTypeConstants jobType;

    @Mock
    private ModelsConstants modelsConstants;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private AuthenticateToken authenticateToken;

    @Mock
    private BulkJobRepository bulkJobRepository;

    @Mock
    private PermissionsNamespaceRepository permissionsNamespaceRepository;

    @Mock
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Mock
    private SctIdHelper sctIdHelper;


    @Mock
    private SecurityController securityController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(bulkSctidService, "repo", repo);
    }

    @Test
    void testPostSctByIds_returnsValidList() throws Exception {
        SctIdRequest request = new SctIdRequest();
        List<Sctid> expectedList = List.of(new Sctid(), new Sctid());
        BulkSctidService realService = new BulkSctidService();
        ReflectionTestUtils.setField(realService, "repo", repo);
        ReflectionTestUtils.setField(realService, "authenticateToken", authenticateToken);
        ReflectionTestUtils.setField(realService, "bulkJobRepository", bulkJobRepository);
        ReflectionTestUtils.setField(realService, "permissionsNamespaceRepository", permissionsNamespaceRepository);
        ReflectionTestUtils.setField(realService, "permissionsSchemeRepository", permissionsSchemeRepository);
        ReflectionTestUtils.setField(realService, "sctIdHelper", sctIdHelper);
        ReflectionTestUtils.setField(realService, "jobType", jobType);
        ReflectionTestUtils.setField(realService, "modelsConstants", modelsConstants);
        ReflectionTestUtils.setField(realService, "securityController", securityController);
        BulkSctidService spyService = Mockito.spy(realService);
        Mockito.doReturn(expectedList).when(spyService).postValidScts(request);
        List<Sctid> result = spyService.postSctByIds(request);
        assertEquals(2, result.size());
        assertSame(expectedList, result);
    }


    @Test
    void testPostSctByIds_shouldThrowCisException() {
        SctIdRequest request = new SctIdRequest();
        request.setSctids("null_value_should_fail");
        CisException thrown = assertThrows(CisException.class, () -> {
            bulkSctidService.postSctByIds(request);
        });
        assertTrue(thrown.getMessage().contains("Not a Valid Sctid"));
    }

    @Test
    void testPostSctByIds_withMockedException() throws Exception {
        SctIdRequest request = new SctIdRequest();
        request.setSctids("anything");
        BulkSctidService mockService = Mockito.mock(BulkSctidService.class);
        when(mockService.postSctByIds(any())).thenCallRealMethod();
        when(mockService.postValidScts(any())).thenThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Mocked error"));
        CisException ex = assertThrows(CisException.class, () -> {
            mockService.postSctByIds(request);
        });
        assertEquals("Mocked error", ex.getMessage());
    }

    @Test
    void testGetSctByIds_shouldReturnList() throws Exception {
        String input = "12345678901";
        List<Sctid> expected = List.of(new Sctid());
        BulkSctidService realService = new BulkSctidService();
        ReflectionTestUtils.setField(realService, "repo", repo);
        ReflectionTestUtils.setField(realService, "sctIdHelper", sctIdHelper);
        BulkSctidService spyService = Mockito.spy(realService);
        Mockito.doReturn(expected).when(spyService).validScts(input);
        List<Sctid> actual = spyService.getSctByIds(input);
        assertEquals(1, actual.size());
        assertSame(expected, actual);
    }

    @Test
    void testGetSctByIds_shouldThrowCisException() throws Exception {
        String input = "invalid_sctid_format";
        BulkSctidService realService = new BulkSctidService();
        ReflectionTestUtils.setField(realService, "repo", repo);
        ReflectionTestUtils.setField(realService, "sctIdHelper", sctIdHelper);
        BulkSctidService spyService = Mockito.spy(realService);
        Mockito.doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid format")).when(spyService).validScts(input);
        CisException ex = assertThrows(CisException.class, () -> {
            spyService.getSctByIds(input);
        });
        assertEquals("Invalid format", ex.getMessage());
    }


    @Test
    void testGetByIds_shouldReturnMatchingSctids() {
        List<String> inputIds = List.of("123", "456");
        Sctid s1 = new Sctid();
        s1.setSctid("123");
        Sctid s2 = new Sctid();
        s2.setSctid("456");
        List<Sctid> expected = List.of(s1, s2);
        when(repo.findBySctidIn(inputIds)).thenReturn(expected);
        List<Sctid> actual = bulkSctidService.getByIds(inputIds);
        assertEquals(expected.size(), actual.size());
        assertEquals("123", actual.get(0).getSctid());
        assertEquals("456", actual.get(1).getSctid());
    }

    @Test
    void testGetByIds_withEmptyList_shouldReturnEmpty() {
        List<String> inputIds = List.of();
        when(repo.findBySctidIn(inputIds)).thenReturn(List.of());
        List<Sctid> result = bulkSctidService.getByIds(inputIds);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByIds_withNullInput_shouldReturnNullOrEmpty() {
        when(repo.findBySctidIn(null)).thenReturn(Collections.emptyList());
        List<Sctid> result = bulkSctidService.getByIds(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidScts_shouldReturnCompleteList_whenSomeIdsAreMissingInDB() throws Exception {
        ArrayList<String> inputList = new ArrayList<>(List.of("111", "222", "333"));
        String input = "111,222,333";
        doNothing().when(bulkSctidService).validSctidCheck(inputList);
        Sctid sct1 = new Sctid();
        sct1.setSctid("111");
        Sctid sct2 = new Sctid();
        sct2.setSctid("222");
        when(bulkSctidService.getByIds(inputList)).thenReturn(new ArrayList<>(List.of(sct1, sct2)));
        Sctid sct3 = new Sctid();
        sct3.setSctid("333");
        when(bulkSctidService.getFreeRecord("333", null)).thenReturn(sct3);
        List<Sctid> result = bulkSctidService.validScts(input);
        assertEquals(3, result.size());
        List<String> ids = result.stream().map(Sctid::getSctid).toList();
        assertTrue(ids.containsAll(List.of("111", "222", "333")));
    }

    @Test
    void testValidScts_shouldNotCallGetFreeRecord_whenAllIdsExist() throws Exception {
        String input = "444,555";
        ArrayList<String> inputList = new ArrayList<>(List.of("444", "555"));
        doNothing().when(bulkSctidService).validSctidCheck(inputList);
        Sctid sct1 = new Sctid();
        sct1.setSctid("444");
        Sctid sct2 = new Sctid();
        sct2.setSctid("555");
        when(bulkSctidService.getByIds(inputList)).thenReturn(new ArrayList<>(List.of(sct1, sct2)));
        List<Sctid> result = bulkSctidService.validScts(input);
        assertEquals(2, result.size());
        verify(bulkSctidService, never()).getFreeRecord(anyString(), any());
    }

    @Test
    void testValidScts_shouldThrow_onInvalidFormat() throws Exception {
        String input = "invalid_id,123";
        doAnswer(invocation -> {
            ArrayList<String> arg = invocation.getArgument(0);
            if (arg.contains("invalid_id")) {
                throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid format");
            }
            return null;
        }).when(bulkSctidService).validSctidCheck(any(ArrayList.class));
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.validScts(input);
        });
        assertEquals("Invalid format", ex.getMessage());
    }

    @Test
    void testValidScts_shouldThrow_whenInputIsNull() {
        assertThrows(NullPointerException.class, () -> {
            bulkSctidService.validScts(null);
        });
    }

    @Test
    void testPostValidScts_allSctidsExist_shouldReturnAll() throws Exception {
        SctIdRequest request = new SctIdRequest();
        request.setSctids("123,456");
        ArrayList<String> parsedList = new ArrayList<>(List.of("123", "456"));
        doNothing().when(bulkSctidService).validSctidCheck(parsedList);
        Sctid s1 = new Sctid();
        s1.setSctid("123");
        Sctid s2 = new Sctid();
        s2.setSctid("456");
        when(bulkSctidService.getByIds(parsedList)).thenReturn(List.of(s1, s2));
        List<Sctid> result = bulkSctidService.postValidScts(request);
        assertEquals(2, result.size());
        verify(bulkSctidService, never()).getFreeRecord(anyString(), any());
    }

    @Test
    void testPostValidScts_someMissing_shouldCallGetFreeRecord() throws Exception {
        SctIdRequest request = new SctIdRequest();
        request.setSctids("111,222,333");
        ArrayList<String> parsedList = new ArrayList<>(List.of("111", "222", "333"));
        doNothing().when(bulkSctidService).validSctidCheck(parsedList);
        Sctid s1 = new Sctid();
        s1.setSctid("111");
        Sctid s2 = new Sctid();
        s2.setSctid("222");
        when(bulkSctidService.getByIds(parsedList)).thenReturn(new ArrayList<>(List.of(s1, s2)));
        Sctid s3 = new Sctid();
        s3.setSctid("333");
        when(bulkSctidService.getFreeRecord(eq("333"), isNull())).thenReturn(s3);
        List<Sctid> result = bulkSctidService.postValidScts(request);
        assertEquals(3, result.size());
        List<String> ids = result.stream().map(Sctid::getSctid).toList();
        assertTrue(ids.containsAll(List.of("111", "222", "333")));
        verify(bulkSctidService).getFreeRecord("333", null);
    }

    @Test
    void testPostValidScts_invalidSctid_shouldThrow() throws Exception {
        SctIdRequest request = new SctIdRequest();
        request.setSctids("bad_id,999");
        doAnswer(invocation -> {
            ArrayList<String> ids = invocation.getArgument(0);
            if (ids.contains("bad_id")) {
                throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid SCTID");
            }
            return null;
        }).when(bulkSctidService).validSctidCheck(any(ArrayList.class));
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.postValidScts(request);
        });
        assertEquals("Invalid SCTID", ex.getMessage());
    }


    @Test
    void testGetFreeRecord_shouldReturnSctid() {
        BulkSctidService realService = new BulkSctidService();
        BulkSctidService spyService = Mockito.spy(realService);
        String sctid = "123456";
        String systemId = null;
        Map<String, Object> fakeMap = new HashMap<>();
        fakeMap.put("id", sctid);
        Sctid fakeSctid = new Sctid();
        fakeSctid.setSctid(sctid);
        doReturn(fakeMap).when(spyService).getNewRecord(sctid, systemId);
        doReturn(fakeSctid).when(spyService).insertSCTIDRecord(fakeMap);
        Sctid result = spyService.getFreeRecord(sctid, systemId);
        assertNotNull(result);
        assertEquals(sctid, result.getSctid());
    }

    @Test
    void testGetFreeRecord_insertReturnsNull_shouldReturnNull() {
        BulkSctidService realService = new BulkSctidService();
        BulkSctidService spyService = Mockito.spy(realService);
        String sctid = "999000";
        String systemId = "SYS1";
        Map<String, Object> fakeMap = new HashMap<>();
        fakeMap.put("id", sctid);
        doReturn(fakeMap).when(spyService).getNewRecord(sctid, systemId);
        doReturn(null).when(spyService).insertSCTIDRecord(fakeMap);
        Sctid result = spyService.getFreeRecord(sctid, systemId);
        assertNull(result, "If insert fails, result should be null");
    }

    @Test
    void testInsertSCTIDRecord_shouldSaveAndReturnSctid() {
        Map<String, Object> sctIdRecord = new HashMap<>();
        sctIdRecord.put("sctid", "999000");
        sctIdRecord.put("sequence", 123L);
        sctIdRecord.put("namespace", 100);
        sctIdRecord.put("partitionId", "10");
        sctIdRecord.put("checkDigit", 1);
        sctIdRecord.put("systemId", "SYS1");
        sctIdRecord.put("status", "AVAILABLE");
        Sctid expected = Sctid.builder().sctid("999000").sequence(123L).namespace(100).partitionId("10").checkDigit(1).systemId("SYS1").status("AVAILABLE").build();
        when(repo.save(any(Sctid.class))).thenReturn(expected);
        Sctid result = bulkSctidService.insertSCTIDRecord(sctIdRecord);
        assertNotNull(result);
        assertEquals("999000", result.getSctid());
        assertEquals("AVAILABLE", result.getStatus());
        verify(repo).save(any(Sctid.class));
    }

    @Test
    void testInsertSCTIDRecord_withMinimalInput_shouldSave() {
        Map<String, Object> sctIdRecord = new HashMap<>();
        sctIdRecord.put("sctid", "123456");
        sctIdRecord.put("status", "AVAILABLE");
        Sctid saved = Sctid.builder().sctid("123456").status("AVAILABLE").build();
        when(repo.save(any(Sctid.class))).thenReturn(saved);
        Sctid result = bulkSctidService.insertSCTIDRecord(sctIdRecord);
        assertEquals("123456", result.getSctid());
        assertEquals("AVAILABLE", result.getStatus());
        verify(repo).save(any(Sctid.class));
    }

    @Test
    void testInsertSCTIDRecord_repoThrows_shouldPropagate() {
        Map<String, Object> sctIdRecord = Map.of("sctid", "888888", "status", "FAILED");
        when(repo.save(any(Sctid.class))).thenThrow(new RuntimeException("DB error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            bulkSctidService.insertSCTIDRecord(sctIdRecord);
        });
        assertEquals("DB error", ex.getMessage());
    }

    @Test
    void testGetSctidBySystemIds_shouldCallRepoAndReturnList() {
        String systemIdStr = "SYS-1, SYS-2";
        Integer namespaceId = 123;
        List<Sctid> mockSctids = List.of(Sctid.builder().sctid("111").systemId("SYS-1").namespace(namespaceId).build(), Sctid.builder().sctid("222").systemId("SYS-2").namespace(namespaceId).build());
        List<String> expectedSystemIds = List.of("SYS-1", "SYS-2");
        when(repo.findBySystemIdInAndNamespace(expectedSystemIds, namespaceId)).thenReturn(mockSctids);
        List<Sctid> result = bulkSctidService.getSctidBySystemIds(systemIdStr, namespaceId);
        assertEquals(2, result.size());
        assertEquals("111", result.get(0).getSctid());
        assertEquals("222", result.get(1).getSctid());
        verify(repo).findBySystemIdInAndNamespace(expectedSystemIds, namespaceId);
    }

    @Test
    void testGetSctidBySystemIds_withEmptySystemIdStr_shouldReturnEmptyList() {
        String systemIdStr = "";
        Integer namespaceId = 100;
        List<Sctid> result = bulkSctidService.getSctidBySystemIds(systemIdStr, namespaceId);
        assertTrue(result.isEmpty());
        verify(repo).findBySystemIdInAndNamespace(List.of(""), namespaceId);
    }

    @Test
    void testGetSctidBySystemIds_withNullSystemIdStr_shouldHandleGracefully() {
        String systemIdStr = null;
        Integer namespaceId = 123;
        assertThrows(NullPointerException.class, () -> {
            bulkSctidService.getSctidBySystemIds(systemIdStr, namespaceId);
        });
    }

    @Test
    void testGetSctidBySystemIds_noMatch_shouldReturnEmptyList() {
        String systemIdStr = "SYS-X,SYS-Y";
        Integer namespaceId = 321;
        List<String> expectedIds = List.of("SYS-X", "SYS-Y");
        when(repo.findBySystemIdInAndNamespace(expectedIds, namespaceId)).thenReturn(Collections.emptyList());
        List<Sctid> result = bulkSctidService.getSctidBySystemIds(systemIdStr, namespaceId);
        assertTrue(result.isEmpty());
        verify(repo).findBySystemIdInAndNamespace(expectedIds, namespaceId);
    }

    @Test
    void testGetSctidBySystemIds_withSpacesAndCommas_shouldTrimProperly() {
        String systemIdStr = "  SYS1 ,  SYS2 ,SYS3  ";
        Integer namespaceId = 55;
        List<String> expected = List.of("SYS1", "SYS2", "SYS3");
        List<Sctid> mockResult = List.of(Sctid.builder().sctid("1").systemId("SYS1").namespace(55).build(), Sctid.builder().sctid("2").systemId("SYS2").namespace(55).build());
        when(repo.findBySystemIdInAndNamespace(expected, namespaceId)).thenReturn(mockResult);
        List<Sctid> result = bulkSctidService.getSctidBySystemIds(systemIdStr, namespaceId);
        assertEquals(2, result.size());
        verify(repo).findBySystemIdInAndNamespace(expected, namespaceId);
    }

    @Test
    void testRegisterSctids_shouldDelegateToRegisterScts_andReturnBulkJob() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        RegistrationDataDTO request = mock(RegistrationDataDTO.class);
        BulkJob expectedJob = new BulkJob();
        doReturn(expectedJob).when(bulkSctidService).registerScts(token, request);
        BulkJob actual = bulkSctidService.registerSctids(token, request);
        assertEquals(expectedJob, actual);
        verify(bulkSctidService).registerScts(token, request);
    }

    @Test
    void testRegisterScts_shouldThrowIfUserNotAuthorized() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        RegistrationDataDTO request = mock(RegistrationDataDTO.class);
        request.setNamespace(0); // or any dummy value
        when(bulkSctidService.isAbleUser("0", token)).thenReturn(false);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.registerScts(token, request);
        });
        assertEquals("No permission for the selected operation.", ex.getMessage());
    }

    @Test
    void testRegisterScts_shouldThrowIfNamespaceMismatch() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        RegistrationRecordsDTO records = mock(RegistrationRecordsDTO.class);
        when(records.getSctid()).thenReturn("1234567890");
        RegistrationDataDTO request = mock(RegistrationDataDTO.class);
        when(request.getNamespace()).thenReturn(100);
        when(request.getRecords()).thenReturn(new RegistrationRecordsDTO[]{records});
        doReturn(true).when(bulkSctidService).isAbleUser("100", token);
        when(sctIdHelper.getNamespace("1234567890")).thenReturn(999);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.registerScts(token, request);
        });
        assertEquals("Differences between generated namespace and input 'namespace'.", ex.getMessage());
    }


    @Test
    void testIsAbleUser_shouldReturnTrue_whenAdminRolePresent() {
        when(authenticateResponseDto.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin"));
        boolean result = bulkSctidService.isAbleUser("123", authenticateResponseDto);
        assertTrue(result);
    }

    @Test
    void testIsAbleUser_shouldReturnTrue_whenHasNamespacePermission() {
        when(authenticateResponseDto.getRoles()).thenReturn(List.of("ROLE_user"));
        doReturn(true).when(bulkSctidService).hasNamespacePermission("123", authenticateResponseDto);
        boolean result = bulkSctidService.isAbleUser("123", authenticateResponseDto);
        assertTrue(result);
    }

    @Test
    void testIsAbleUser_shouldReturnFalse_whenNoAdminOrNamespacePermission() {
        when(authenticateResponseDto.getRoles()).thenReturn(List.of("ROLE_user"));
        doReturn(false).when(bulkSctidService).hasNamespacePermission("123", authenticateResponseDto);
        boolean result = bulkSctidService.isAbleUser("123", authenticateResponseDto);
        assertFalse(result);
    }

    @Test
    void testHasNamespacePermission_shouldReturnFalse_whenNoMatch() {
        String namespace = "123";
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("random");
        when(token.getRoles()).thenReturn(Collections.singletonList("ROLE_othergroup"));
        PermissionsNamespace perm = new PermissionsNamespace();
        perm.setNamespace(123);
        perm.setRole("user");
        perm.setUsername("john");
        when(permissionsNamespaceRepository.findByNamespace(123)).thenReturn(Collections.singletonList(perm));
        boolean result = bulkSctidService.hasNamespacePermission(namespace, token);
        assertFalse(result);
    }

    @Test
    void testGenerateSctids_shouldThrowIfUserNotPermitted() {
        SCTIDBulkGenerationRequestDto request = new SCTIDBulkGenerationRequestDto();
        request.setNamespace(100);
        request.setPartitionId("01");
        request.setQuantity(10);
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        doReturn(false).when(bulkSctidService).isAbleUser("100", token);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.generateSctids(token, request);
        });
        assertEquals("user has no permission for the selected operation.", ex.getMessage());
    }

    @Test
    void testGenerateSctids_shouldThrowIfQuantityInvalid() {
        SCTIDBulkGenerationRequestDto request = new SCTIDBulkGenerationRequestDto();
        request.setNamespace(100);
        request.setPartitionId("11");
        request.setQuantity(0);
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        doReturn(true).when(bulkSctidService).isAbleUser("100", token);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.generateSctids(token, request);
        });
        assertEquals("quantity value must be positive number", ex.getMessage());
    }

    @Test
    void testGenerateSctids_shouldThrowIfNamespacePartitionMismatch() {
        SCTIDBulkGenerationRequestDto request = new SCTIDBulkGenerationRequestDto();
        request.setNamespace(0);
        request.setPartitionId("11");
        request.setQuantity(5);
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        doReturn(true).when(bulkSctidService).isAbleUser("0", token);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.generateSctids(token, request);
        });
        assertEquals("Namespace and partitionId parameters are not consistent.", ex.getMessage());
    }

    @Test
    void testGenerateSctids_shouldThrowIfSystemIdCountMismatch() {
        SCTIDBulkGenerationRequestDto request = new SCTIDBulkGenerationRequestDto();
        request.setNamespace(100);
        request.setPartitionId("10");
        request.setQuantity(5);
        request.setSystemIds(Arrays.asList("SYS1", "SYS2"));
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        doReturn(true).when(bulkSctidService).isAbleUser("100", token);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.generateSctids(token, request);
        });
        assertEquals("SystemIds quantity is not equal to input 'quantity'.", ex.getMessage());
    }

    @Test
    void testGenerateSctids_shouldCreateBulkJobSuccessfully() throws Exception {
        SCTIDBulkGenerationRequestDto request = new SCTIDBulkGenerationRequestDto();
        request.setNamespace(100);
        request.setPartitionId("10");
        request.setQuantity(2);
        request.setSystemIds(Arrays.asList("SYS1", "SYS2"));
        request.setSoftware("testSoft");
        request.setComment("test comment");
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("testUser");
        doReturn(true).when(bulkSctidService).isAbleUser("100", token);
        BulkJob savedJob = new BulkJob();
        savedJob.setName("GENERATE_SCTIDS");
        savedJob.setStatus("0");
        when(bulkJobRepository.save(any(BulkJob.class))).thenReturn(savedJob);
        BulkJobResponseDto response = bulkSctidService.generateSctids(token, request);
        assertNotNull(response);
        assertEquals("GENERATE_SCTIDS", response.getName());

    }

    @Test
    void testIsSchemeAbleUser_shouldReturnTrueWhenHasSchemePermission() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setRoles(List.of("GROUP_other-role"));
        doReturn(true).when(bulkSctidService).hasSchemePermission("SNOMEDID", token);
        boolean result = bulkSctidService.isSchemeAbleUser("SNOMEDID", token);
        assertTrue(result, "User with scheme permission should be allowed");
    }

    @Test
    void testIsSchemeAbleUser_shouldReturnFalseWhenNoPermission() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setRoles(List.of("GROUP_some-random-role"));
        doReturn(false).when(bulkSctidService).hasSchemePermission("SNOMEDID", token);
        boolean result = bulkSctidService.isSchemeAbleUser("SNOMEDID", token);
        assertFalse(result, "User without scheme permission or admin group should not be allowed");
    }

    @Test
    void testHasSchemePermission_returnsFalse_whenNoMatch() {
        String scheme = "SNOMEDID";
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("john");
        token.setRoles(List.of("GROUP_namespace-xyz", "GROUP_test-nomatch"));
        PermissionsScheme perm = new PermissionsScheme();
        perm.setRole("group");
        perm.setUsername("another-group");
        when(permissionsSchemeRepository.findByScheme(scheme)).thenReturn(List.of(perm));
        boolean result = bulkSctidService.hasSchemePermission(scheme, token);
        assertFalse(result, "Should return false when no user or group match found");
    }

    @Test
    void testHasSchemePermission_returnsFalse_whenSchemeIsFalse() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("john");
        token.setRoles(List.of("GROUP_test-group"));
        boolean result = bulkSctidService.hasSchemePermission("false", token);
        assertFalse(result, "Should return false when schemeName is 'false'");
        verifyNoInteractions(permissionsSchemeRepository);
    }

    @Test
    void shouldThrowUnauthorized_whenUserNotAllowed() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        BulkSctRequestDTO dto = mock(BulkSctRequestDTO.class);
        when(dto.getNamespace()).thenReturn(100);
        when(dto.getSctids()).thenReturn(new String[]{"12345678901"});
        doReturn(false).when(bulkSctidService).isAbleUser("100", token);
        CisException ex = assertThrows(CisException.class, () -> bulkSctidService.releaseSctid(token, dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("no permission"));
    }

    @Test
    void shouldThrowAccepted_whenSctidsEmpty() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        BulkSctRequestDTO dto = mock(BulkSctRequestDTO.class);
        when(dto.getNamespace()).thenReturn(100);
        when(dto.getSctids()).thenReturn(new String[0]);
        doReturn(true).when(bulkSctidService).isAbleUser("100", token);
        CisException ex = assertThrows(CisException.class, () -> bulkSctidService.releaseSctid(token, dto));
        assertEquals(HttpStatus.ACCEPTED, ex.getStatus());
        assertTrue(ex.getMessage().contains("cannot be empty"));
    }

    @Test
    void shouldThrowAccepted_whenNamespaceMismatch() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        BulkSctRequestDTO dto = mock(BulkSctRequestDTO.class);
        when(dto.getNamespace()).thenReturn(100);
        when(dto.getSctids()).thenReturn(new String[]{"12345678901"});
        doReturn(true).when(bulkSctidService).isAbleUser("100", token);
        when(sctIdHelper.getNamespace("12345678901")).thenReturn(999); // mismatch
        CisException ex = assertThrows(CisException.class, () -> bulkSctidService.releaseSctid(token, dto));
        assertEquals(HttpStatus.ACCEPTED, ex.getStatus());
        assertTrue(ex.getMessage().contains("Difference between generated namespace"));
    }

    @Test
    void shouldReturnReservedBulkJob_whenUserIsAuthorized() throws CisException {
        AuthenticateResponseDto authToken = mock(AuthenticateResponseDto.class);
        SCTIDBulkReservationRequestDto dto = mock(SCTIDBulkReservationRequestDto.class);
        when(dto.getNamespace()).thenReturn(100);
        when(authToken.getName()).thenReturn("testUser");
        doReturn(true).when(bulkSctidService).isAbleUser("100", authToken);
        BulkJob expectedJob = new BulkJob();
        expectedJob.setName("RESERVE_SCTIDS");
        doReturn(expectedJob).when(bulkSctidService).bulkReserveSctids(dto, "testUser");
        BulkJob result = bulkSctidService.reserveSctids(authToken, dto);
        assertNotNull(result);
        assertEquals("RESERVE_SCTIDS", result.getName());
    }

    @Test
    void shouldThrowForbidden_whenUserNotAuthorized() {
        AuthenticateResponseDto authToken = mock(AuthenticateResponseDto.class);
        SCTIDBulkReservationRequestDto dto = mock(SCTIDBulkReservationRequestDto.class);
        when(dto.getNamespace()).thenReturn(100);
        doReturn(false).when(bulkSctidService).isAbleUser("100", authToken);
        CisException ex = assertThrows(CisException.class, () -> bulkSctidService.reserveSctids(authToken, dto));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertTrue(ex.getMessage().contains("No permission"));
    }

    @Test
    void reserveSctids_shouldReturnBulkJob_whenUserIsAuthorized() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SCTIDBulkReservationRequestDto dto = mock(SCTIDBulkReservationRequestDto.class);
        when(dto.getNamespace()).thenReturn(123);
        when(token.getName()).thenReturn("mock-user");
        BulkJob expectedJob = new BulkJob();
        expectedJob.setName("RESERVE_SCTIDS");
        doReturn(true).when(bulkSctidService).isAbleUser("123", token);
        doReturn(expectedJob).when(bulkSctidService).bulkReserveSctids(dto, "mock-user");
        BulkJob actualJob = bulkSctidService.reserveSctids(token, dto);
        assertNotNull(actualJob);
        assertEquals("RESERVE_SCTIDS", actualJob.getName());
        verify(bulkSctidService).bulkReserveSctids(dto, "mock-user");
    }

    @Test
    void reserveSctids_shouldThrowException_whenUserIsUnauthorized() {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SCTIDBulkReservationRequestDto dto = mock(SCTIDBulkReservationRequestDto.class);
        when(dto.getNamespace()).thenReturn(456);
        doReturn(false).when(bulkSctidService).isAbleUser("456", token);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.reserveSctids(token, dto);
        });
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertTrue(ex.getMessage().contains("No permission"));
    }

    @Test
    void bulkReserveSctids_shouldReturnBulkJob_whenValidRequest() throws Exception {
        SCTIDBulkReservationRequestDto dto = mock(SCTIDBulkReservationRequestDto.class);
        when(dto.getNamespace()).thenReturn(0);
        when(dto.getPartitionId()).thenReturn("0");
        when(dto.getExpirationDate()).thenReturn(String.valueOf(LocalDateTime.now().plusDays(1)));
        when(dto.getQuantity()).thenReturn(10);
        when(dto.getSoftware()).thenReturn("TestSoftware");
        when(dto.getComment()).thenReturn("TestComment");
        BulkJob savedJob = new BulkJob();
        savedJob.setName("RESERVE_SCTIDS");
        when(bulkJobRepository.save(any(BulkJob.class))).thenReturn(savedJob);
        BulkJob result = bulkSctidService.bulkReserveSctids(dto, "testUser");
        assertNotNull(result);
        assertEquals("RESERVE_SCTIDS", result.getName());
    }

    @Test
    void bulkReserveSctids_shouldThrowException_whenNamespacePartitionMismatch() {
        SCTIDBulkReservationRequestDto dto = mock(SCTIDBulkReservationRequestDto.class);
        when(dto.getNamespace()).thenReturn(0);
        when(dto.getPartitionId()).thenReturn("1");
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.bulkReserveSctids(dto, "user");
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("parameters are not consistent"));
    }

    @Test
    void bulkReserveSctids_shouldThrowException_whenQuantityIsZero() {
        SCTIDBulkReservationRequestDto dto = mock(SCTIDBulkReservationRequestDto.class);
        when(dto.getNamespace()).thenReturn(0);
        when(dto.getPartitionId()).thenReturn("0");
        when(dto.getQuantity()).thenReturn(0);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSctidService.bulkReserveSctids(dto, "user");
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("cannot be lower to 1"));
    }


}




