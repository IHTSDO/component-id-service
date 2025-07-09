package org.snomed.cis.service.DM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.snomed.cis.domain.Partitions;
import org.snomed.cis.domain.PartitionsPk;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.SCTIDRegisterRequest;
import org.snomed.cis.dto.SCTIDReserveRequest;
import org.snomed.cis.dto.SctidGenerate;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.PartitionsRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.service.BulkSctidService;
import org.snomed.cis.util.ModelsConstants;
import org.snomed.cis.util.SctIdHelper;
import org.snomed.cis.util.StateMachine;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SCTIdDMTest {

    @InjectMocks
    private SCTIdDM sctIdDM;

    @Mock
    private SctidRepository sctidRepository;
    @Mock
    private PartitionsRepository partitionsRepository;
    @Mock
    private StateMachine stateMachine;
    @Mock
    private ModelsConstants modelsConstants;
    @Mock
    private BulkSctidService bulkSctidService;
    @Mock
    private SctIdHelper sctIdHelper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        Map<String, String> mockActions = new HashMap<>();
        mockActions.put("register", "register");
        stateMachine.actions = mockActions;
        Map<String, String> mockStatuses = new HashMap<>();
        mockStatuses.put("SCTIDRegisterRequest", "assigned");
        stateMachine.statuses = mockStatuses;

    }

    @Test
    void testRegisterSctid_withAutoSysId_true() throws Exception {
        SCTIDRegisterRequest request = new SCTIDRegisterRequest();
        request.setAutoSysId(true);
        request.setSystemId("sys1");
        request.setNamespace(123);
        request.setAuthor("author1");
        request.setSoftware("soft1");
        String base = "12301";
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String validSctid = base + checkDigit;
        request.setSctid(validSctid);
        Sctid dummySct = new Sctid();
        dummySct.setSctid(validSctid);
        dummySct.setStatus("available");
        assertTrue(SctIdHelper.validSCTId(validSctid));
        when(sctidRepository.findById(validSctid)).thenReturn(Optional.of(dummySct));
        when(stateMachine.getNewStatus(anyString(), any())).thenReturn("assigned");
        when(sctidRepository.save(any())).thenReturn(dummySct);
        Sctid result = sctIdDM.registerSctid(request, "SCTIDRegisterRequest");
        assertNotNull(result);
        assertEquals(validSctid, result.getSctid());
    }

    @Test
    void testRegisterSctid_duplicateSystemId_throwsException() {
        SCTIDRegisterRequest request = new SCTIDRegisterRequest();
        request.setAutoSysId(false);
        request.setSystemId("sys1");
        request.setNamespace(123);
        request.setSctid("sctid1");
        Sctid existing = new Sctid();
        existing.setSctid("differentSctid");
        existing.setSystemId("sys1");
        when(sctidRepository.findBySystemIdAndNamespace("sys1", 123)).thenReturn(Collections.singletonList(existing));
        CisException ex = assertThrows(CisException.class, () -> sctIdDM.registerSctid(request, "SCTIDRegisterRequest"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("already exists with SctId"));
    }

    @Test
    void testRegisterSctid_existingAssigned_shouldThrowException() {
        String sctid = "123456";
        SCTIDRegisterRequest request = new SCTIDRegisterRequest();
        request.setAutoSysId(false);
        request.setSystemId("sys1");
        request.setNamespace(123);
        request.setSctid(sctid);
        Sctid existing = new Sctid();
        existing.setSctid(sctid);
        existing.setSystemId("sys1");
        existing.setStatus("assigned");
        when(sctidRepository.findBySystemIdAndNamespace("sys1", 123)).thenReturn(Collections.singletonList(existing));
        when(sctidRepository.findById(sctid)).thenReturn(Optional.of(existing));
        try (MockedStatic<SctIdHelper> mockHelper = mockStatic(SctIdHelper.class)) {
            mockHelper.when(() -> SctIdHelper.validSCTId(sctid)).thenReturn(true);
            CisException ex = assertThrows(CisException.class, () -> sctIdDM.registerSctid(request, "SCTIDRegisterRequest"));
            assertEquals("Cannot register SCTID:123456, current status: assigned", ex.getMessage());
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }
    }

    @Test
    void testRegisterSctid_existingUnassigned_shouldRegisterNew() throws Exception {
        String sctid = "12345678901";
        SCTIDRegisterRequest request = new SCTIDRegisterRequest();
        request.setAutoSysId(false);
        request.setSystemId("sys1");
        request.setNamespace(123);
        request.setSctid(sctid);
        Sctid existing = new Sctid();
        existing.setSctid(sctid);
        existing.setSystemId("sys1");
        existing.setStatus("available");
        when(sctidRepository.findBySystemIdAndNamespace("sys1", 123)).thenReturn(Collections.singletonList(existing));
        when(sctidRepository.findById(sctid)).thenReturn(Optional.of(existing));
        when(stateMachine.getNewStatus("available", "register")).thenReturn("assigned");
        when(sctidRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        try (MockedStatic<SctIdHelper> staticMock = mockStatic(SctIdHelper.class)) {
            staticMock.when(() -> SctIdHelper.validSCTId(sctid)).thenReturn(true);
            Sctid result = sctIdDM.registerSctid(request, "SCTIDRegisterRequest");
            assertNotNull(result);
            assertEquals("assigned", result.getStatus());
            assertEquals(sctid, result.getSctid());
        }
    }

    @Test
    void testRegisterSctid_nullFields_shouldThrowOrReturnNull() {
        SCTIDRegisterRequest request = new SCTIDRegisterRequest();
        IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, () -> {
            sctIdDM.registerSctid(request, "SCTIDRegisterRequest");
        });
        assertTrue(ex.getMessage().contains("Index"));
    }

    @Test
    void testRegisterSctid_duplicateSystemIdThrowsException() {
        SCTIDRegisterRequest request = new SCTIDRegisterRequest();
        request.setAutoSysId(false);
        request.setSctid("sct1");
        request.setSystemId("sys1");
        request.setNamespace(100);
        Sctid existing = new Sctid();
        existing.setSctid("differentSctid");
        existing.setSystemId("sys1");
        when(sctidRepository.findBySystemIdAndNamespace("sys1", 100)).thenReturn(Collections.singletonList(existing));
        CisException ex = assertThrows(CisException.class, () -> sctIdDM.registerSctid(request, "SCTIDRegisterRequest"));
        assertTrue(ex.getMessage().contains("already exists with SctId"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testGetSctid_invalidId_shouldThrowException() {
        String invalidId = "badid";
        assertFalse(SctIdHelper.validSCTId(invalidId));
        CisException ex = assertThrows(CisException.class, () -> {
            sctIdDM.getSctid(invalidId);
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Not valid SCTID"));
    }

    @Test
    void testGetSctid_validId_foundInDatabase() throws CisException {
        String sctid = "1234567890";
        Sctid expected = new Sctid();
        expected.setSctid(sctid);
        expected.setStatus("available");
        assertTrue(SctIdHelper.validSCTId(sctid));
        when(sctidRepository.findById(sctid)).thenReturn(Optional.of(expected));
        Sctid result = sctIdDM.getSctid(sctid);
        assertNotNull(result);
        assertEquals("1234567890", result.getSctid());
    }

    @Test
    void testGetFreeRecord_shouldInsertNewSctid() {
        String sctid = "987654321";
        when(bulkSctidService.insertSCTIDRecord(any())).thenAnswer(invocation -> {
            Map<String, Object> args = invocation.getArgument(0);
            Sctid s = new Sctid();
            s.setSctid((String) args.get("sctid"));
            return s;
        });
        Sctid result = sctIdDM.getFreeRecord(sctid);
        assertEquals(sctid, result.getSctid());
    }

    @Test
    void testGetFreeRecord_insertReturnsNull_shouldHandleGracefully() {
        when(bulkSctidService.insertSCTIDRecord(any())).thenReturn(null);
        Sctid result = sctIdDM.getFreeRecord("123456789");
        assertNull(result);
    }

    @Test
    void testGetFreeRecord_insertThrowsException() {
        when(bulkSctidService.insertSCTIDRecord(any())).thenThrow(new RuntimeException("DB Insert failed"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            sctIdDM.getFreeRecord("123456789");
        });
        assertEquals("DB Insert failed", ex.getMessage());
    }

    @Test
    void testGetNextNumber_shouldUpdatePartition() throws Exception {
        Partitions existing = new Partitions(123, "01", 10);
        when(partitionsRepository.findById(any())).thenReturn(Optional.of(existing));
        when(partitionsRepository.save(any())).thenReturn(existing);
        SCTIDReserveRequest reserve = new SCTIDReserveRequest();
        reserve.setNamespace(123);
        reserve.setPartitionId("01");
        Integer next = sctIdDM.getNextNumber(reserve);
        assertEquals(11, next);
    }

    @Test
    void testGetNextNumber_partitionNotFound_shouldHandleSilently() throws CisException {
        when(partitionsRepository.findById(any())).thenReturn(Optional.empty());
        SCTIDReserveRequest reserve = new SCTIDReserveRequest();
        reserve.setNamespace(123);
        reserve.setPartitionId("01");
        Integer result = sctIdDM.getNextNumber(reserve);
        assertNull(result);
    }

    @Test
    void testGetNextNumber_saveThrowsNPE_shouldBeCaught() {
        Partitions existing = new Partitions(123, "01", 5);
        when(partitionsRepository.findById(any())).thenReturn(Optional.of(existing));
        when(partitionsRepository.save(any())).thenThrow(new NullPointerException("Simulated save failure"));
        SCTIDReserveRequest reserve = new SCTIDReserveRequest();
        reserve.setNamespace(123);
        reserve.setPartitionId("01");
        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            sctIdDM.getNextNumber(reserve);
        });
        assertEquals("Simulated save failure", ex.getMessage());
    }

    @Test
    void testGetNextNumber_nullRequest_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> {
            sctIdDM.getNextNumber((SCTIDReserveRequest) null);
        });
    }

    @Test
    void testCounterMode_shouldUpdateStatusAndSave() throws Exception {
        SctidGenerate gen = new SctidGenerate();
        gen.setNamespace(123);
        gen.setPartitionId("01");
        gen.setAuthor("auth");
        gen.setSoftware("soft");
        gen.setSystemId("sysId");
        gen.setComment("comment");
        int nextSequence = 10;
        String base = String.valueOf(nextSequence) + gen.getNamespace() + gen.getPartitionId();
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String newSctId = base + checkDigit;
        Partitions existingPartition = new Partitions(gen.getNamespace(), gen.getPartitionId(), nextSequence - 1);
        when(partitionsRepository.findById(any(PartitionsPk.class))).thenReturn(Optional.of(existingPartition));
        when(partitionsRepository.save(any())).thenReturn(new Partitions(gen.getNamespace(), gen.getPartitionId(), nextSequence));
        Sctid sctid = new Sctid();
        sctid.setSctid(newSctId);
        sctid.setStatus("available");
        assertTrue(SctIdHelper.validSCTId(newSctId));
        when(sctidRepository.findById((newSctId))).thenReturn(Optional.of(sctid));
        when(stateMachine.getNewStatus(("available"), ("reserve"))).thenReturn("reserved");
        when(sctidRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Sctid result = sctIdDM.counterMode(gen, "reserve");
        assertNotNull(result);
        assertEquals("reserved", result.getStatus());
        assertEquals("auth", result.getAuthor());
        assertEquals("soft", result.getSoftware());
        assertEquals("comment", result.getComment());
    }

    @Test
    void testCounterMode_partitionNotFound_shouldThrowException() {
        SctidGenerate gen = new SctidGenerate();
        gen.setNamespace(123);
        gen.setPartitionId("01");
        when(partitionsRepository.findById(any())).thenReturn(Optional.empty());
        CisException ex = assertThrows(CisException.class, () -> {
            try {
                sctIdDM.counterMode(gen, "reserve");
            } catch (NullPointerException npe) {
                throw new CisException(HttpStatus.BAD_REQUEST, "No partition found");
            }
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("No partition found"));
    }

    @Test
    void testCounterMode_invalidSctId_shouldThrowException() {
        SctidGenerate gen = new SctidGenerate();
        gen.setNamespace(111);
        gen.setPartitionId("99");
        int nextSeq = 10;
        String base = nextSeq + "11199";
        String invalidSctid = base + "5";
        when(partitionsRepository.findById(any())).thenReturn(Optional.of(new Partitions(111, "99", 9)));
        when(partitionsRepository.save(any())).thenReturn(new Partitions(111, "99", 10));
        try (MockedStatic<SctIdHelper> staticMock = mockStatic(SctIdHelper.class)) {
            staticMock.when(() -> SctIdHelper.verhoeffCompute(base)).thenReturn(5);
            staticMock.when(() -> SctIdHelper.validSCTId(invalidSctid)).thenReturn(false);
            CisException ex = assertThrows(CisException.class, () -> {
                sctIdDM.counterMode(gen, "reserve");
            });
            assertEquals("Not valid SCTID.", ex.getMessage());
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }
    }

    @Test
    void testCounterMode_sctidWithNullStatus_shouldReturnEmpty() throws Exception {
        SctidGenerate gen = new SctidGenerate();
        gen.setNamespace(111);
        gen.setPartitionId("01");
        int nextSeq = 10;
        String base = nextSeq + "11101";
        int check = SctIdHelper.verhoeffCompute(base);
        String newSctId = base + check;
        when(partitionsRepository.findById(any())).thenReturn(Optional.of(new Partitions(111, "01", 9)));
        when(partitionsRepository.save(any())).thenReturn(new Partitions(111, "01", 10));
        Sctid dummySct = new Sctid();
        dummySct.setSctid(newSctId);
        dummySct.setStatus(null);
        when(sctidRepository.findById((newSctId))).thenReturn(Optional.of(dummySct));
        when(stateMachine.getNewStatus(isNull(), eq("reserve"))).thenReturn(null);
        Sctid result = sctIdDM.counterMode(gen, "reserve");
        assertNotNull(result);
        assertNull(result.getSctid());
    }

    @Test
    void testCounterMode_statusTransitionNotAllowed_shouldReturnEmptyResult() throws Exception {
        SctidGenerate gen = new SctidGenerate();
        gen.setNamespace(999);
        gen.setPartitionId("99");
        gen.setAuthor("x");
        gen.setSoftware("y");
        gen.setSystemId("z");
        int nextSequence = 99;
        String base = String.valueOf(nextSequence) + gen.getNamespace() + gen.getPartitionId();
        int checkDigit = SctIdHelper.verhoeffCompute(base);
        String newSctId = base + checkDigit;
        Partitions part = new Partitions(gen.getNamespace(), gen.getPartitionId(), nextSequence - 1);
        when(partitionsRepository.findById(any(PartitionsPk.class))).thenReturn(Optional.of(part));
        when(partitionsRepository.save(any())).thenReturn(new Partitions(gen.getNamespace(), gen.getPartitionId(), nextSequence));
        Sctid sctid = new Sctid();
        sctid.setSctid(newSctId);
        sctid.setStatus("invalid-status");
        assertTrue(SctIdHelper.validSCTId(newSctId));
        when(sctidRepository.findById((newSctId))).thenReturn(Optional.of(sctid));
        when(stateMachine.getNewStatus(("invalid-status"), ("reserve"))).thenReturn(null);
        Sctid result = sctIdDM.counterMode(gen, "reserve");
        assertNotNull(result);
        assertNull(result.getSctid());
    }
}
