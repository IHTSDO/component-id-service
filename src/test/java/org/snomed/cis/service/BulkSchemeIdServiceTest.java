package org.snomed.cis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkJobRepository;
import org.snomed.cis.repository.BulkSchemeIdRepository;
import org.snomed.cis.repository.PermissionsSchemeRepository;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.snomed.cis.util.JobTypeConstants;
import org.snomed.cis.util.SctIdHelper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkSchemeIdServiceTest {
    @Spy
    @InjectMocks
    private BulkSchemeIdService bulkSchemeIdService;

    @Mock
    private BulkSchemeIdRepository bulkSchemeIdRepository;

    @Mock
    private AuthenticateToken authenticateToken;

    @Mock
    private SchemeIdBaseRepository schemeIdBaseRepository;

    @Mock
    private BulkJobRepository bulkJobRepository;

    @Mock
    private BulkSctidService bulkSctidService;

    @Mock
    private SctIdHelper sctIdHelper;

    @Mock
    private SchemeIdService schemeIdService;

    @Mock
    private PermissionsSchemeRepository permissionsSchemeRepository;

    @Test
    void testGetSchemeIds_shouldThrow_whenUnauthorized() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(false);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.getSchemeIds(token, SchemeName.SNOMEDID, "11111111"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("No permission"));
    }

    @Test
    void testGetSchemeIds_shouldThrow_whenSchemeIdEmpty() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(true);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.getSchemeIds(token, SchemeName.SNOMEDID, "   "));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("SchemeId is null"));
    }

    @Test
    void testGetFreeRecord_shouldReturnSchemeId() throws Exception {
        String schemeName = "SNOMEDID";
        String diffSchemeId = "22222222";
        String systemId = null;
        String autoSysId = "true";
        Map<String, Object> fakeRecord = new HashMap<>();
        fakeRecord.put("schemeId", diffSchemeId);
        fakeRecord.put("scheme", schemeName);
        SchemeId expected = new SchemeId();
        expected.setSchemeId(diffSchemeId);
        expected.setScheme(schemeName);
        doReturn(fakeRecord).when(bulkSchemeIdService).getNewRecord((schemeName), (diffSchemeId), (systemId));
        doReturn(expected).when(bulkSchemeIdService).insertSchemeIdRecord((fakeRecord));
        SchemeId result = bulkSchemeIdService.getFreeRecord(schemeName, diffSchemeId, systemId, autoSysId);
        assertNotNull(result);
        assertEquals("22222222", result.getSchemeId());
        assertEquals("SNOMEDID", result.getScheme());
    }

    @Test
    void testGetFreeRecord_shouldThrow_whenInsertFails() throws CisException {
        String schemeName = "SNOMEDID";
        String diffSchemeId = "XYZ";
        String systemId = null;
        String autoSysId = "true";
        Map<String, Object> mockRecord = new HashMap<>();
        doReturn(mockRecord).when(bulkSchemeIdService).getNewRecord((schemeName), (diffSchemeId), (systemId));
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Insert failed")).when(bulkSchemeIdService).insertSchemeIdRecord((mockRecord));
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.getFreeRecord(schemeName, diffSchemeId, systemId, autoSysId));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        assertTrue(ex.getMessage().contains("Insert failed"));
    }

    @Test
    void testInsertSchemeIdRecord_shouldReturnSavedSchemeId() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("scheme", "SNOMEDID");
        input.put("schemeId", "22222222");
        input.put("sequence", 1234);
        input.put("checkDigit", 2);
        input.put("systemId", "SYS-001");
        input.put("status", "AVAILABLE");
        input.put("author", "test-author");
        input.put("software", "test-software");
        input.put("jobId", 999);
        input.put("expirationDate", LocalDateTime.now());
        input.put("created_at", LocalDateTime.now());
        input.put("modified_at", LocalDateTime.now());
        SchemeId expected = SchemeId.builder().scheme("SNOMEDID").schemeId("22222222").sequence(1234).checkDigit(2).systemId("SYS-001").status("AVAILABLE").author("test-author").software("test-software").jobId(999).expirationDate((LocalDateTime) input.get("expirationDate")).created_at((LocalDateTime) input.get("created_at")).modified_at((LocalDateTime) input.get("modified_at")).build();
        when(bulkSchemeIdRepository.save(any(SchemeId.class))).thenReturn(expected);
        SchemeId result = bulkSchemeIdService.insertSchemeIdRecord(input);
        assertNotNull(result);
        assertEquals("22222222", result.getSchemeId());
        assertEquals("SNOMEDID", result.getScheme());
        assertEquals("AVAILABLE", result.getStatus());
        assertEquals("SYS-001", result.getSystemId());
    }

    @Test
    void testGetNewRecord_withSystemId() {
        String schemeName = "SNOMEDID";
        String diffSchemeId = "12345678";
        String systemId = "SYS-999";
        when(sctIdHelper.getSequence(diffSchemeId)).thenReturn(123L);
        when(sctIdHelper.getCheckDigit(diffSchemeId)).thenReturn(7);
        Map<String, Object> result = bulkSchemeIdService.getNewRecord(schemeName, diffSchemeId, systemId);
        assertEquals(5, result.size());
        assertEquals("SNOMEDID", result.get("scheme"));
        assertEquals("12345678", result.get("schemeId"));
        assertEquals(123L, result.get("sequence"));
        assertEquals(7, result.get("checkDigit"));
        assertEquals("SYS-999", result.get("systemId"));
    }

    @Test
    void testGetNewRecord_withoutSystemId_shouldGenerateGuid() {
        String schemeName = "CTV3ID";
        String diffSchemeId = "87654321";
        when(sctIdHelper.getSequence(diffSchemeId)).thenReturn(456L);
        when(sctIdHelper.getCheckDigit(diffSchemeId)).thenReturn(2);
        Map<String, Object> result = bulkSchemeIdService.getNewRecord(schemeName, diffSchemeId, null);
        assertEquals(5, result.size());
        assertEquals("CTV3ID", result.get("scheme"));
        assertEquals("87654321", result.get("schemeId"));
        assertEquals(456L, result.get("sequence"));
        assertEquals(2, result.get("checkDigit"));
    }

    @Test
    void testGenerateSchemeIds_shouldReturnSavedJob() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkGenerationRequestDto dto = new SchemeIdBulkGenerationRequestDto();
        dto.setQuantity(2);
        dto.setSoftware("TestSoft");
        dto.setComment("Test comment");
        dto.setSystemIds(new String[]{"SYS1", "SYS2"});
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("testUser");
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(true);
        ArgumentCaptor<BulkJob> jobCaptor = ArgumentCaptor.forClass(BulkJob.class);
        BulkJob savedJob = new BulkJob();
        savedJob.setId(101);
        savedJob.setName(JobTypeConstants.GENERATE_SCHEMEIDS);
        savedJob.setStatus("0");
        when(bulkJobRepository.save(any(BulkJob.class))).thenReturn(savedJob);
        BulkJob result = bulkSchemeIdService.generateSchemeIds(token, schemeName, dto);
        assertNotNull(result);
        assertEquals(Integer.valueOf(101), result.getId());
        assertEquals(JobTypeConstants.GENERATE_SCHEMEIDS, result.getName());
        assertEquals("0", result.getStatus());
        verify(bulkJobRepository).save(jobCaptor.capture());
        BulkJob captured = jobCaptor.getValue();
        assertTrue(captured.getRequest().contains("SNOMEDID"));
        assertEquals("0", captured.getStatus());
        assertEquals(JobTypeConstants.GENERATE_SCHEMEIDS, captured.getName());
    }

    @Test
    void testGenerateSchemeIds_shouldThrow_whenQuantityMismatchWithSystemIds() throws CisException {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkGenerationRequestDto dto = new SchemeIdBulkGenerationRequestDto();
        dto.setQuantity(3);
        dto.setSystemIds(new String[]{"SYS1", "SYS2"});
        dto.setSoftware("TestSoft");
        dto.setComment("Mismatch test");
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(true);
        CisException exception = assertThrows(CisException.class, () -> {
            bulkSchemeIdService.generateSchemeIds(token, schemeName, dto);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("SystemIds quantity is not equal"));
    }

    @Test
    void testGenerateSchemeIds_shouldThrow_whenUserUnauthorized() throws CisException {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkGenerationRequestDto dto = new SchemeIdBulkGenerationRequestDto();
        dto.setQuantity(1);
        dto.setSystemIds(new String[]{"SYS1"});
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(false);
        CisException exception = assertThrows(CisException.class, () -> bulkSchemeIdService.generateSchemeIds(token, schemeName, dto));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertTrue(exception.getMessage().contains("No permission"));
    }

    @Test
    void testGenerateSchemeIds_withNullSystemIds_shouldEnableAutoSysId() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkGenerationRequestDto dto = new SchemeIdBulkGenerationRequestDto();
        dto.setQuantity(2);
        dto.setSoftware("AutoSoft");
        dto.setComment("No System Ids");
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("autoUser");
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(true);
        ArgumentCaptor<BulkJob> captor = ArgumentCaptor.forClass(BulkJob.class);
        BulkJob saved = new BulkJob();
        saved.setId(202);
        saved.setName(JobTypeConstants.GENERATE_SCHEMEIDS);
        saved.setStatus("0");
        when(bulkJobRepository.save(any(BulkJob.class))).thenReturn(saved);
        BulkJob result = bulkSchemeIdService.generateSchemeIds(token, schemeName, dto);
        assertNotNull(result);
        verify(bulkJobRepository).save(captor.capture());
        String json = captor.getValue().getRequest();
        assertTrue(json.contains("\"autoSysId\":true"));
    }

    @Test
    void testRegisterSchemeIds_shouldReturnBulkJob() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkRegisterRequestDto request = new SchemeIdBulkRegisterRequestDto();
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        BulkJob expectedJob = new BulkJob();
        expectedJob.setId(303);
        expectedJob.setName("REGISTER_SCHEMEIDS");
        expectedJob.setStatus("1");
        doReturn(expectedJob).when(bulkSchemeIdService).registerBulkSchemeIds((token), (schemeName), (request));
        BulkJob result = bulkSchemeIdService.registerSchemeIds(token, schemeName, request);
        assertNotNull(result);
        assertEquals(Integer.valueOf(303), result.getId());
        assertEquals("REGISTER_SCHEMEIDS", result.getName());
        assertEquals("1", result.getStatus());
        verify(bulkSchemeIdService).registerBulkSchemeIds(token, schemeName, request);
    }

    @Test
    void testRegisterSchemeIds_shouldThrowCisException_whenUserNotAllowed() throws Exception {
        SchemeName schemeName = SchemeName.CTV3ID;
        SchemeIdBulkRegisterRequestDto request = new SchemeIdBulkRegisterRequestDto();
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        doThrow(new CisException(HttpStatus.UNAUTHORIZED, "User not permitted")).when(bulkSchemeIdService).registerBulkSchemeIds((token), (schemeName), (request));
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.registerSchemeIds(token, schemeName, request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("User not permitted", ex.getMessage());
        verify(bulkSchemeIdService).registerBulkSchemeIds(token, schemeName, request);
    }

    @Test
    void testRegisterSchemeIds_shouldReturnValidResponse() throws Exception {
        SchemeName schemeName = SchemeName.CTV3ID;
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkRegisterRequestDto request = new SchemeIdBulkRegisterRequestDto();
        BulkJob expectedJob = new BulkJob();
        expectedJob.setId(201);
        expectedJob.setName("REGISTER_SCHEMEIDS");
        expectedJob.setStatus("0");
        doReturn(expectedJob).when(bulkSchemeIdService).registerBulkSchemeIds(token, schemeName, request);
        BulkJob result = bulkSchemeIdService.registerSchemeIds(token, schemeName, request);
        assertNotNull(result);
        assertEquals(Integer.valueOf(201), result.getId());
        assertEquals("REGISTER_SCHEMEIDS", result.getName());
        assertEquals("0", result.getStatus());
    }

    @Test
    void testRegisterSchemeIds_shouldThrow_whenRequestIsNull() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        doThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid request")).when(bulkSchemeIdService).registerBulkSchemeIds(eq(token), eq(schemeName), isNull());
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.registerSchemeIds(token, schemeName, null));
        assertEquals("Invalid request", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testRegisterSchemeIds_shouldThrow_onInternalError() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkRegisterRequestDto request = new SchemeIdBulkRegisterRequestDto();
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error")).when(bulkSchemeIdService).registerBulkSchemeIds(token, schemeName, request);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.registerSchemeIds(token, schemeName, request));
        assertEquals("Internal Error", ex.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }

    @Test
    void testRegisterSchemeIds_shouldReturnSavedJob() throws Exception {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeRegistrationRecord rec = new SchemeRegistrationRecord();
        rec.setSchemeId("12345678");
        List<SchemeRegistrationRecord> records = new ArrayList<>();
        records.add(rec);
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();
        dto.setRecords(records);
        dto.setSoftware("TestSoft");
        dto.setComment("test");
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("testUser");
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(true);
        ArgumentCaptor<BulkJob> jobCaptor = ArgumentCaptor.forClass(BulkJob.class);
        BulkJob savedJob = new BulkJob();
        savedJob.setId(123);
        savedJob.setName(JobTypeConstants.REGISTER_SCHEMEIDS);
        savedJob.setStatus("0");
        when(bulkJobRepository.save(any(BulkJob.class))).thenReturn(savedJob);
        BulkJob result = bulkSchemeIdService.registerSchemeIds(token, schemeName, dto);
        assertNotNull(result);
        assertEquals(Integer.valueOf(123), result.getId());
        assertEquals(JobTypeConstants.REGISTER_SCHEMEIDS, result.getName());
        assertEquals("0", result.getStatus());
        verify(bulkJobRepository).save(jobCaptor.capture());
        BulkJob captured = jobCaptor.getValue();
        assertTrue(captured.getRequest().contains("SNOMEDID"));
        assertTrue(captured.getRequest().contains("12345678"));
    }

    @Test
    void testRegisterSchemeIds_shouldThrowException_whenUserHasNoPermission() throws CisException {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();
        SchemeRegistrationRecord rec = new SchemeRegistrationRecord();
        rec.setSchemeId("99999999");
        List<SchemeRegistrationRecord> records = new ArrayList<>();
        records.add(rec);
        dto.setRecords(records);
        dto.setSoftware("TestSoft");
        dto.setComment("test");
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.toString()).thenReturn("mocked-token");
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(false);
        CisException exception = assertThrows(CisException.class, () -> bulkSchemeIdService.registerSchemeIds(token, schemeName, dto));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("No permission for the selected operation", exception.getMessage());
    }

    @Test
    void testRegisterSchemeIds_shouldThrowException_whenRecordsListIsNull() throws CisException {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();
        dto.setRecords(null);
        dto.setSoftware("TestSoft");
        dto.setComment("test");
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.toString()).thenReturn("mocked-token");
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(true);
        CisException exception = assertThrows(CisException.class, () -> bulkSchemeIdService.registerSchemeIds(token, schemeName, dto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Records property cannot be empty.", exception.getMessage());
    }

    @Test
    void testRegisterSchemeIds_shouldThrowException_whenRecordsListIsEmpty() throws CisException {
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkRegisterRequestDto dto = new SchemeIdBulkRegisterRequestDto();
        dto.setRecords(new ArrayList<>());
        dto.setSoftware("TestSoft");
        dto.setComment("test");
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.toString()).thenReturn("mocked-token");
        when(schemeIdService.isAbleUser(("SNOMEDID"), (token))).thenReturn(true);
        CisException exception = assertThrows(CisException.class, () -> bulkSchemeIdService.registerSchemeIds(token, schemeName, dto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Records property cannot be empty.", exception.getMessage());
    }

    @Test
    void reserveSchemeIds_shouldReturnBulkJob() throws Exception {
        AuthenticateResponseDto mockToken = mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkReserveRequestDto requestDto = new SchemeIdBulkReserveRequestDto();
        BulkJob expectedJob = new BulkJob();
        expectedJob.setId(123);
        doReturn(expectedJob).when(bulkSchemeIdService).reserveBulkSchemeIds(mockToken, schemeName, requestDto);
        BulkJob actualJob = bulkSchemeIdService.reserveSchemeIds(mockToken, schemeName, requestDto);
        assertNotNull(actualJob);
        assertEquals(expectedJob.getId(), actualJob.getId());
        verify(bulkSchemeIdService).reserveBulkSchemeIds(mockToken, schemeName, requestDto);
    }

    @Test
    void reserveSchemeIds_shouldThrowCisException_whenReserveBulkFails() throws Exception {
        AuthenticateResponseDto mockToken = mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkReserveRequestDto requestDto = new SchemeIdBulkReserveRequestDto();
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Reserve failed")).when(bulkSchemeIdService).reserveBulkSchemeIds(mockToken, schemeName, requestDto);
        CisException exception = assertThrows(CisException.class, () -> {
            bulkSchemeIdService.reserveSchemeIds(mockToken, schemeName, requestDto);
        });
        assertEquals("Reserve failed", exception.getMessage());
        verify(bulkSchemeIdService).reserveBulkSchemeIds(mockToken, schemeName, requestDto);
    }

    @Test
    void reserveSchemeIds_shouldThrowException_whenRequestIsNull() throws Exception {
        AuthenticateResponseDto mockToken = mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Request cannot be null")).when(bulkSchemeIdService).reserveBulkSchemeIds(eq(mockToken), eq(schemeName), isNull());
        CisException exception = assertThrows(CisException.class, () -> {
            bulkSchemeIdService.reserveSchemeIds(mockToken, schemeName, null);
        });
        assertEquals("Request cannot be null", exception.getMessage());
    }

    @Test
    void reserveSchemeIds_shouldThrowException_whenReserveBulkFails() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkReserveRequestDto requestDto = new SchemeIdBulkReserveRequestDto();
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Reserve failed")).when(bulkSchemeIdService).reserveBulkSchemeIds(token, SchemeName.SNOMEDID, requestDto);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSchemeIdService.reserveSchemeIds(token, SchemeName.SNOMEDID, requestDto);
        });
        assertEquals("Reserve failed", ex.getMessage());
    }

    @Test
    void reserveSchemeIds_shouldWorkForAllSchemeTypes() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkReserveRequestDto request = new SchemeIdBulkReserveRequestDto();
        BulkJob mockJob = new BulkJob();
        for (SchemeName schemeName : SchemeName.values()) {
            doReturn(mockJob).when(bulkSchemeIdService).reserveBulkSchemeIds(token, schemeName, request);
            BulkJob result = bulkSchemeIdService.reserveSchemeIds(token, schemeName, request);
            assertNotNull(result);
        }
    }

    @Test
    void reserveBulkSchemeIds_shouldReturnBulkJob_whenValidInput() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("admin");
        SchemeIdBulkReserveRequestDto request = new SchemeIdBulkReserveRequestDto();
        request.setQuantity(5);
        request.setSoftware("TestSoftware");
        request.setExpirationDate(String.valueOf(LocalDate.now().plusDays(5)));
        request.setComment("Test comment");
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        when(bulkJobRepository.save(any(BulkJob.class))).thenAnswer(invocation -> {
            BulkJob saved = invocation.getArgument(0);
            saved.setId(101);
            return saved;
        });
        BulkJob result = bulkSchemeIdService.reserveBulkSchemeIds(token, SchemeName.SNOMEDID, request);
        assertNotNull(result);
        assertEquals(Integer.valueOf(101), result.getId());
        assertEquals("0", result.getStatus());
        assertEquals("Reserve SchemeIds", result.getName());
    }

    @Test
    void reserveBulkSchemeIds_shouldThrow_whenQuantityIsInvalid() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("admin");
        SchemeIdBulkReserveRequestDto request = new SchemeIdBulkReserveRequestDto();
        request.setQuantity(0); // or null
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.reserveBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Quantity property cannot be lower to 1.", ex.getMessage());
    }

    @Test
    void reserveBulkSchemeIds_shouldThrow_whenUserHasNoPermission() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("user");
        SchemeIdBulkReserveRequestDto request = new SchemeIdBulkReserveRequestDto();
        request.setQuantity(5);
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(false);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.reserveBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getMessage());
    }

    @Test
    void deprecateSchemeIds_shouldReturnBulkJob_whenValidInput() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        BulkJob expectedJob = new BulkJob();
        expectedJob.setId(777);
        doReturn(expectedJob).when(bulkSchemeIdService).deprecateBulkSchemeIds(token, schemeName, request);
        BulkJob actualJob = bulkSchemeIdService.deprecateSchemeIds(token, schemeName, request);
        assertNotNull(actualJob);
        assertEquals(Integer.valueOf(777), actualJob.getId());
        verify(bulkSchemeIdService).deprecateBulkSchemeIds(token, schemeName, request);
    }

    @Test
    void deprecateSchemeIds_shouldThrowCisException_whenDeprecateBulkFails() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Deprecation failed")).when(bulkSchemeIdService).deprecateBulkSchemeIds(token, schemeName, request);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.deprecateSchemeIds(token, schemeName, request));
        assertEquals("Deprecation failed", ex.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }

    @Test
    void deprecateSchemeIds_shouldThrowNPE_whenTokenIsNull() {
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> bulkSchemeIdService.deprecateSchemeIds(null, SchemeName.SNOMEDID, request));
        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Cannot invoke"));
    }

    @Test
    void deprecateSchemeIds_shouldThrow_whenRequestIsNull() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        doThrow(new CisException(HttpStatus.BAD_REQUEST, "Request cannot be null")).when(bulkSchemeIdService).deprecateBulkSchemeIds(token, SchemeName.SNOMEDID, null);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.deprecateSchemeIds(token, SchemeName.SNOMEDID, null));
        assertEquals("Request cannot be null", ex.getMessage());
    }

    @Test
    void deprecateSchemeIds_shouldWorkForAllSchemeNames() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        BulkJob dummyJob = new BulkJob();
        for (SchemeName schemeName : SchemeName.values()) {
            doReturn(dummyJob).when(bulkSchemeIdService).deprecateBulkSchemeIds(token, schemeName, request);
            BulkJob result = bulkSchemeIdService.deprecateSchemeIds(token, schemeName, request);
            assertNotNull(result);
        }
    }

    @Test
    void deprecateBulkSchemeIds_shouldReturnBulkJob_whenValidRequest() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("admin");
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSoftware("testTool");
        request.setComment("testing");
        request.setSchemeIds(List.of("SCHEME123", "SCHEME456"));
        BulkJob expectedJob = new BulkJob();
        expectedJob.setId(123);
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        doReturn(expectedJob).when(bulkSchemeIdService).createJob(any(BulkSchemeIdUpdate.class), eq("deprecate"));
        BulkJob result = bulkSchemeIdService.deprecateBulkSchemeIds(token, SchemeName.SNOMEDID, request);
        assertNotNull(result);
        assertEquals(Integer.valueOf(123), result.getId());
    }

    @Test
    void deprecateBulkSchemeIds_shouldThrow_whenSchemeIdsEmpty() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("admin");
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSchemeIds(Collections.emptyList()); // Empty list
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSchemeIdService.deprecateBulkSchemeIds(token, SchemeName.SNOMEDID, request);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("SchemeIds property cannot be empty.", ex.getMessage());
    }

    @Test
    void deprecateBulkSchemeIds_shouldThrow_whenUserNotAuthorized() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("user");
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSchemeIds(List.of("SCHEME123"));
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(false);
        CisException ex = assertThrows(CisException.class, () -> {
            bulkSchemeIdService.deprecateBulkSchemeIds(token, SchemeName.SNOMEDID, request);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getMessage());
    }

    @Test
    void deprecateBulkSchemeIds_shouldThrowNPE_whenTokenIsNull() {
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSchemeIds(List.of("SCHEME123"));
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            bulkSchemeIdService.deprecateBulkSchemeIds(null, SchemeName.SNOMEDID, request);
        });
        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Cannot invoke"));
    }

    private BulkSchemeIdUpdate validUpdate() {
        BulkSchemeIdUpdate update = new BulkSchemeIdUpdate();
        update.setSchemeIds(List.of("S123", "S124"));
        update.setSoftware("Tool");
        update.setAuthor("admin");
        update.setScheme("SNOMEDID");
        update.setComment("Test");
        update.setType("DEPRECATE_SCHEMEIDS");
        update.setModel("SCHEME_ID");
        return update;
    }

    @Test
    void createJob_shouldSetNameToDeprecate_whenFunctionTypeIsDeprecate() throws Exception {
        BulkSchemeIdUpdate update = validUpdate();
        when(bulkJobRepository.save(any(BulkJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BulkJob result = bulkSchemeIdService.createJob(update, "deprecate");
        assertEquals(JobTypeConstants.DEPRECATE_SCHEMEIDS, result.getName());
        assertEquals("0", result.getStatus());
        assertNotNull(result.getRequest());
    }

    @Test
    void createJob_shouldSetNameToRelease_whenFunctionTypeIsRelease() throws Exception {
        BulkSchemeIdUpdate update = validUpdate();
        BulkJob savedJob = new BulkJob();
        savedJob.setId(2);
        when(bulkJobRepository.save(any(BulkJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BulkJob result = bulkSchemeIdService.createJob(update, "release");
        assertEquals(JobTypeConstants.RELEASE_SCHEMEIDS, result.getName());
    }

    @Test
    void createJob_shouldSetNameToPublish_whenFunctionTypeIsPublish() throws Exception {
        BulkSchemeIdUpdate update = validUpdate();
        when(bulkJobRepository.save(any(BulkJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BulkJob result = bulkSchemeIdService.createJob(update, "publish");
        assertEquals(JobTypeConstants.PUBLISH_SCHEMEIDS, result.getName());
        assertEquals("0", result.getStatus());
        assertNotNull(result.getRequest());
    }

    @Test
    void createJob_shouldThrowCisException_whenJsonFails() {
        BulkSchemeIdUpdate badUpdate = new BulkSchemeIdUpdate() {
            private final Object self = this;

            public Object getSelf() {
                return self;
            }
        };
        badUpdate.setScheme("SCHEME");
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.createJob(badUpdate, "deprecate"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void releaseSchemeIds_shouldReturnBulkJob_whenValidRequest() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        BulkJob expectedJob = new BulkJob();
        expectedJob.setId(55);
        doReturn(expectedJob).when(bulkSchemeIdService).releaseBulkSchemeIds(token, schemeName, request);
        BulkJob result = bulkSchemeIdService.releaseSchemeIds(token, schemeName, request);
        assertNotNull(result);
        assertEquals(Integer.valueOf(55), result.getId());
        verify(bulkSchemeIdService).releaseBulkSchemeIds(token, schemeName, request);
    }

    @Test
    void releaseSchemeIds_shouldThrowCisException_whenReleaseFails() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeName schemeName = SchemeName.SNOMEDID;
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        doThrow(new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "Release failed")).when(bulkSchemeIdService).releaseBulkSchemeIds(token, schemeName, request);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.releaseSchemeIds(token, schemeName, request));
        assertEquals("Release failed", ex.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }

    @Test
    void releaseSchemeIds_shouldThrowNPE_whenTokenIsNull() {
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> bulkSchemeIdService.releaseSchemeIds(null, SchemeName.SNOMEDID, request));
        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Cannot invoke"));
    }

    @Test
    void releaseSchemeIds_shouldThrowException_whenRequestIsNull() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        doThrow(new CisException(HttpStatus.BAD_REQUEST, "Request cannot be null")).when(bulkSchemeIdService).releaseBulkSchemeIds(token, SchemeName.SNOMEDID, null);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.releaseSchemeIds(token, SchemeName.SNOMEDID, null));
        assertEquals("Request cannot be null", ex.getMessage());
    }

    @Test
    void releaseSchemeIds_shouldWorkWithAllSchemeNames() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        BulkJob dummyJob = new BulkJob();
        dummyJob.setId(99);
        for (SchemeName schemeName : SchemeName.values()) {
            doReturn(dummyJob).when(bulkSchemeIdService).releaseBulkSchemeIds(token, schemeName, request);
            BulkJob result = bulkSchemeIdService.releaseSchemeIds(token, schemeName, request);
            assertNotNull(result);
            assertEquals(Integer.valueOf(99), result.getId());
        }
    }

    @Test
    void releaseBulkSchemeIds_shouldReturnBulkJob_whenValidInput() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("admin");
        SchemeIdBulkDeprecateRequestDto request = mock(SchemeIdBulkDeprecateRequestDto.class);
        when(request.getSchemeIds()).thenReturn(List.of("SCHEME001", "SCHEME002"));
        when(request.getSoftware()).thenReturn("TestSoftware");
        when(request.getComment()).thenReturn("TestComment");
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        BulkJob expectedJob = new BulkJob();
        expectedJob.setId(999);
        doReturn(expectedJob).when(bulkSchemeIdService).createJob(any(BulkSchemeIdUpdate.class), eq("release"));
        BulkJob result = bulkSchemeIdService.releaseBulkSchemeIds(token, SchemeName.SNOMEDID, request);
        assertNotNull(result);
        assertEquals(Integer.valueOf(999), result.getId());
    }

    @Test
    void releaseBulkSchemeIds_shouldThrow_whenSchemeIdsIsNull() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkDeprecateRequestDto request = mock(SchemeIdBulkDeprecateRequestDto.class);
        when(request.getSchemeIds()).thenReturn(null);
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.releaseBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals("SchemeIds property cannot be empty.", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void releaseBulkSchemeIds_shouldThrow_whenSchemeIdsIsEmpty() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkDeprecateRequestDto request = mock(SchemeIdBulkDeprecateRequestDto.class);
        when(request.getSchemeIds()).thenReturn(Collections.emptyList());
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.releaseBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals("SchemeIds property cannot be empty.", ex.getMessage());
    }

    @Test
    void releaseBulkSchemeIds_shouldThrow_whenCreateJobFails() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        when(token.getName()).thenReturn("admin");
        SchemeIdBulkDeprecateRequestDto request = mock(SchemeIdBulkDeprecateRequestDto.class);
        when(request.getSchemeIds()).thenReturn(List.of("SCHEME001"));
        when(request.getSoftware()).thenReturn("TestSoftware");
        when(request.getComment()).thenReturn("Some comment");
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        doThrow(new CisException(HttpStatus.BAD_REQUEST, "Job creation failed")).when(bulkSchemeIdService).createJob(any(), eq("release"));
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.releaseBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals("Job creation failed", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void publishSchemeIds_shouldReturnBulkJob_whenValidInput() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        SchemeName schemeName = SchemeName.SNOMEDID;
        BulkJob expected = new BulkJob();
        expected.setId(321);
        doReturn(expected).when(bulkSchemeIdService).publishBulkSchemeIds(token, schemeName, request);
        BulkJob result = bulkSchemeIdService.publishSchemeIds(token, schemeName, request);
        assertNotNull(result);
        assertEquals(Integer.valueOf(321), result.getId());
        verify(bulkSchemeIdService).publishBulkSchemeIds(token, schemeName, request);
    }

    @Test
    void publishSchemeIds_shouldThrowCisException_whenPublishFails() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        SchemeName schemeName = SchemeName.SNOMEDID;
        doThrow(new CisException(HttpStatus.BAD_REQUEST, "Publish failed")).when(bulkSchemeIdService).publishBulkSchemeIds(token, schemeName, request);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.publishSchemeIds(token, schemeName, request));
        assertEquals("Publish failed", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void publishSchemeIds_shouldThrowNPE_whenTokenIsNull() {
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> bulkSchemeIdService.publishSchemeIds(null, SchemeName.SNOMEDID, request));
        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Cannot invoke"));
    }

    @Test
    void publishBulkSchemeIds_shouldReturnBulkJob_whenValid() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSchemeIds(List.of("SCHEME001"));
        request.setSoftware("TestSoftware");
        request.setComment("Publishing");
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        BulkJob mockJob = new BulkJob();
        mockJob.setId(111);
        doReturn(mockJob).when(bulkSchemeIdService).createJob(any(BulkSchemeIdUpdate.class), eq("publish"));
        BulkJob result = bulkSchemeIdService.publishBulkSchemeIds(token, SchemeName.SNOMEDID, request);
        assertNotNull(result);
        assertEquals(Integer.valueOf(111), result.getId());
    }

    @Test
    void publishBulkSchemeIds_shouldThrow_whenSchemeIdsIsNull() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSchemeIds(null);
        request.setSoftware("Tool");
        request.setComment("Test");
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.publishBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals("SchemeIds property cannot be empty.", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void publishBulkSchemeIds_shouldThrow_whenSchemeIdsIsEmpty() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("admin");
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSchemeIds(Collections.emptyList());
        request.setSoftware("Tool");
        request.setComment("Test");
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.publishBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals("SchemeIds property cannot be empty.", ex.getMessage());
    }

    @Test
    void publishBulkSchemeIds_shouldThrow_whenUserIsNotAuthorized() throws CisException {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("unauthorizedUser");
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSchemeIds(List.of("SCHEME001"));
        request.setSoftware("Tool");
        request.setComment("No Access");
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(false);
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.publishBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals("No permission for the selected operation", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void publishBulkSchemeIds_shouldThrow_whenCreateJobFails() throws Exception {
        AuthenticateResponseDto token = mock(AuthenticateResponseDto.class);
        token.setName("admin");
        SchemeIdBulkDeprecateRequestDto request = new SchemeIdBulkDeprecateRequestDto();
        request.setSchemeIds(List.of("SCHEME001"));
        request.setSoftware("Tool");
        request.setComment("Fails during creation");
        when(schemeIdService.isAbleUser("SNOMEDID", token)).thenReturn(true);
        doThrow(new CisException(HttpStatus.BAD_REQUEST, "Job creation failed")).when(bulkSchemeIdService).createJob(any(BulkSchemeIdUpdate.class), eq("publish"));
        CisException ex = assertThrows(CisException.class, () -> bulkSchemeIdService.publishBulkSchemeIds(token, SchemeName.SNOMEDID, request));
        assertEquals("Job creation failed", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}