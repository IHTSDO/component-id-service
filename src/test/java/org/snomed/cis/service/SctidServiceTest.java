package org.snomed.cis.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.SchemeIdBaseRepository;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.service.DM.SCTIdDM;
import org.snomed.cis.util.SctIdHelper;
import org.snomed.cis.util.StateMachine;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SctidServiceTest {

    @InjectMocks
    private SctidService sctidService;

    @Mock
    private BulkSctidService bulkSctidService;

    @Mock
    private AuthenticateResponseDto authenticateResponseDto;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query mockQuery;

    @Mock
    private SctIdHelper sctIdHelper;

    @Mock
    private SctidRepository sctidRepository;

    @Mock
    private StateMachine stateMachine;

    @Mock
    private SCTIdDM sctIdDM;

    @Mock
    private SchemeIdBaseRepository schemeIdRepository;



    private Sctid createTestSctid(String sctid) {
        return new Sctid(
                sctid,
                1L,
                1234567,
                "10",
                6,
                "1df22661-4b02-4a34-9fb2-c24e2e99408c",
                "Available",
                "test-author",
                "test-software",
                LocalDateTime.now(),
                "test-comment",
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private SchemeId createTestSchemeId(String schemeId, String systemId) {
        return new SchemeId(
                "test-scheme",
                schemeId,
                1,
                5,
                systemId,
                "ACTIVE",
                "test-author",
                "test-software",
                LocalDateTime.now().plusYears(1),
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private DeprecateSctRequestDTO createDeprecateRequest(String sctid, Integer namespace, String software, String comment) {
        DeprecateSctRequestDTO request = new DeprecateSctRequestDTO();
        request.setSctid(sctid);
        request.setNamespace(namespace);
        request.setSoftware(software);
        request.setComment(comment);
        return request;
    }

    private void injectStateMachine(String action, String value) {
        stateMachine = new StateMachine();
        stateMachine.actions = new HashMap<>();
        stateMachine.actions.put(action, value);
        ReflectionTestUtils.setField(sctidService, "stateMachine", stateMachine);
    }


    @Test
    void getSchemeIds_WithSystemId_ReturnsFilteredResults() {
        when(entityManager.createNativeQuery(contains("WHERE systemId='test-system'"), eq(SchemeId.class)))
                .thenReturn(mockQuery);
        when(mockQuery.getResultList())
                .thenReturn(List.of(createTestSchemeId("scheme1","test-system"), createTestSchemeId("scheme2","test-system")));

        List<SchemeId> result = sctidService.getSchemeIds("test-system", "10", "0");
        assertEquals(2, result.size());
    }

    @Test
    void getSchemeIds_WithPagination_AppliesLimit() {
        when(entityManager.createNativeQuery(contains("limit 10"), eq(SchemeId.class)))
                .thenReturn(mockQuery);
        when(mockQuery.getResultList())
                .thenReturn(List.of(createTestSchemeId("scheme1","test-system"), createTestSchemeId("scheme2","test-system")));


        List<SchemeId> result = sctidService.getSchemeIds("test-system", "10", "0");
        assertEquals(2, result.size());
    }

    @Test
    void getSchemeIds_WithSkip_AppliesOffset() {
        List<SchemeId> fullSet = IntStream.range(0, 15)
                .mapToObj(i -> createTestSchemeId(("scheme" + i), "test-system"))
                .toList();

        when(entityManager.createNativeQuery(not(contains("limit")), eq(SchemeId.class)))
                .thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(fullSet);


        List<SchemeId> result = sctidService.getSchemeIds("test-system", "10", "10");
        assertEquals(5, result.size());
    }

    @Test
    void getSchemeIds_EmptySystemId_ReturnsAll() {
        when(entityManager.createNativeQuery(not(contains("WHERE")), eq(SchemeId.class)))
                .thenReturn(mockQuery);
        when(mockQuery.getResultList())
                .thenReturn(List.of(createTestSchemeId("scheme1","test-system-1"), createTestSchemeId("scheme2","test-system-2")));

        List<SchemeId> result = sctidService.getSchemeIds("", "10", "0");
        assertEquals(2, result.size());
    }

    @Test
    void getSchemeIds_InvalidLimit_ThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class,
                () -> sctidService.getSchemeIds("test-system", "abc", "0"));
    }

    @Test
    void getSct_UnauthorizedUser_ThrowsException() {
        when(bulkSctidService.isAbleUser("false", authenticateResponseDto)).thenReturn(false);
        CisException exception = assertThrows(CisException.class,
                () -> sctidService.getSct(authenticateResponseDto, "10", "5", "1234567"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void getSct_ValidInputs_ReturnsSctidList() throws CisException {
        when(bulkSctidService.isAbleUser("false", authenticateResponseDto)).thenReturn(true);
        when(entityManager.createNativeQuery(anyString(), eq(Sctid.class)))
                .thenReturn(mockQuery);
        doReturn(List.of(createTestSctid("1234567"), createTestSctid("7654321")))
                .when(mockQuery).getResultList();

        List<Sctid> result = sctidService.getSct(authenticateResponseDto, "10", "0", "1234567");
        assertEquals(2, result.size());
    }

    @Test
    void getSct_EmptyResult_ReturnsEmptyList() throws CisException {
        when(bulkSctidService.isAbleUser("false", authenticateResponseDto)).thenReturn(true);
        when(entityManager.createNativeQuery(anyString(), eq(Sctid.class)))
                .thenReturn(mockQuery);
        doReturn(List.of()).when(mockQuery).getResultList();

        List<Sctid> result = sctidService.getSct(authenticateResponseDto, "10", "0", "1234567");
        assertEquals(0, result.size());
    }

    @Test
    void getSct_NullNamespace_ReturnsResults() throws CisException {
        when(bulkSctidService.isAbleUser("false", authenticateResponseDto)).thenReturn(true);
        when(entityManager.createNativeQuery(anyString(), eq(Sctid.class)))
                .thenReturn(mockQuery);
        doReturn(List.of(createTestSctid("1234567")))
                .when(mockQuery).getResultList();

        List<Sctid> result = sctidService.getSct(authenticateResponseDto, "10", "0", null);
        assertEquals(1, result.size());
    }

    @Test
    void getSct_BoundaryPagination_ReturnsCorrectResults() throws CisException {
        when(bulkSctidService.isAbleUser("false", authenticateResponseDto)).thenReturn(true);

        when(entityManager.createNativeQuery(anyString(), eq(Sctid.class))).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            Query mockQueryBuilding = mock(Query.class);

            if (sql.contains("limit 10")) {
                when(mockQueryBuilding.getResultList()).thenReturn(
                        IntStream.range(0, 10)
                                .mapToObj(i -> createTestSctid("1234567"))
                                .toList()
                );
            } else {
                when(mockQueryBuilding.getResultList()).thenReturn(
                        IntStream.range(0, 15)
                                .mapToObj(i -> createTestSctid("1234567"))
                                .toList()
                );
            }
            return mockQueryBuilding;
        });

        // Test for limited - pagination
        List<Sctid> result = sctidService.getSct(authenticateResponseDto, "10", "0", "1234567");
        assertEquals(10, result.size());

        // Test for results skipped
        List<Sctid> skippedResult = sctidService.getSct(authenticateResponseDto, "10", "10", "1234567");
        assertEquals(5, skippedResult.size());
    }

    @Test
    void getSct_InvalidLimitAndSkipValues_ThrowsNumberFormatException() {
        when(bulkSctidService.isAbleUser("false", authenticateResponseDto)).thenReturn(true);

        assertThrows(NumberFormatException.class,
                () -> sctidService.getSct(authenticateResponseDto, "abc", "5", "1234567"));
        assertThrows(NumberFormatException.class,
                () -> sctidService.getSct(authenticateResponseDto, "5", "abc", "1234567"));
    }

    @Test
    void getSctWithId_WithNamespaceAndAuthorizedUser_ReturnsResponse() throws CisException {
        String testSctid = "1234567890";
        String includeAdditionalIds = "false";
        int namespace = 1234;

        Sctid mockSctid = createTestSctid(testSctid);

        when(sctIdHelper.getNamespace(testSctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(sctidRepository.findById(testSctid)).thenReturn(Optional.of(mockSctid));

        try (MockedStatic<SctIdHelper> mockedHelper = mockStatic(SctIdHelper.class)) {
            mockedHelper.when(() -> SctIdHelper.validSCTId(testSctid)).thenReturn(true);

            SctWithSchemeResponseDTO result = sctidService.getSctWithId(authenticateResponseDto, testSctid, includeAdditionalIds);

            assertNotNull(result);
            assertEquals(testSctid, result.getSctid());
            assertEquals(mockSctid.getSystemId(), result.getSystemId());
        }
    }


    @Test
    void getSctWithId_WithNullNamespace_SkipsPermissionCheck() throws CisException {
        String testSctid = "1234567890";
        String includeAdditionalIds = "false";

        Sctid mockSctid = createTestSctid(testSctid);

        // skip permission check
        when(sctIdHelper.getNamespace(testSctid)).thenReturn(null);
        when(sctidRepository.findById(testSctid)).thenReturn(Optional.of(mockSctid));

        try (MockedStatic<SctIdHelper> mockedHelper = mockStatic(SctIdHelper.class)) {
            mockedHelper.when(() -> SctIdHelper.validSCTId(testSctid)).thenReturn(true);

            SctWithSchemeResponseDTO result = sctidService.getSctWithId(authenticateResponseDto, testSctid, includeAdditionalIds);

            assertNotNull(result);
            assertEquals(testSctid, result.getSctid());
            assertEquals(mockSctid.getSystemId(), result.getSystemId());
        }
    }

    @Test
    void getSctWithId_WithNamespaceAndUnauthorizedUser_ThrowsException() {
        String testSctid = "1234567890";
        String includeAdditionalIds = "false";
        int namespace = 1234;


        when(sctIdHelper.getNamespace(testSctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(false);

        CisException exception = assertThrows(CisException.class, () -> {
            sctidService.getSctWithId(authenticateResponseDto, testSctid, includeAdditionalIds);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("No permission for the selected operation", exception.getMessage());
    }

    @Test
    void getSctWithId_WithAdditionalIds_ReturnsResponseWithSchemeList() throws CisException {
        String testSctid = "1234567890";
        String includeAdditionalIds = "true";
        int namespace = 1234;

        Sctid mockSctid = createTestSctid(testSctid);
        List<SchemeId> mockSchemeList = List.of(
                createTestSchemeId("scheme1", mockSctid.getSystemId()),
                createTestSchemeId("scheme2", mockSctid.getSystemId())
        );

        when(sctIdHelper.getNamespace(testSctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(sctidRepository.findById(testSctid)).thenReturn(Optional.of(mockSctid));
        when(entityManager.createNativeQuery(anyString(), eq(SchemeId.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockSchemeList);

        try (MockedStatic<SctIdHelper> mockedHelper = mockStatic(SctIdHelper.class)) {
            mockedHelper.when(() -> SctIdHelper.validSCTId(testSctid)).thenReturn(true);

            SctWithSchemeResponseDTO result = sctidService.getSctWithId(authenticateResponseDto, testSctid, includeAdditionalIds);

            assertNotNull(result);
            assertEquals(testSctid, result.getSctid());
            assertEquals(2, result.getAdditionalIds().size());
            assertEquals("scheme1", result.getAdditionalIds().get(0).getSchemeId());
        }
    }

    @Test
    void checkSctid_ValidInput_ReturnsResponseDTO() throws CisException {
        String testSctid = "1234567890";

        CheckSctidResponseDTO expectedResponse = new CheckSctidResponseDTO();
        expectedResponse.setIsSCTIDValid("true");
        expectedResponse.setSctid(testSctid);
        expectedResponse.setErrorMessage(null);

        try (MockedStatic<SctIdHelper> mockedHelper = mockStatic(SctIdHelper.class)) {
            mockedHelper.when(() -> SctIdHelper.checkSctid(testSctid)).thenReturn(expectedResponse);

            CheckSctidResponseDTO result = sctidService.checkSctid(testSctid);

            assertNotNull(result);
            assertTrue(Boolean.parseBoolean(result.getIsSCTIDValid()));
            assertEquals(testSctid, result.getSctid());
            assertNull(result.getErrorMessage());
        }
    }


    @Test
    void getSctWithSystemId_AuthorizedUser_ReturnsFirstSctid() throws CisException {
        Integer namespaceId = 1234;
        String systemId = "system-id-1234";
        Sctid mockSctid = createTestSctid("1234567890");

        when(bulkSctidService.isAbleUser(String.valueOf(namespaceId), authenticateResponseDto)).thenReturn(true);
        when(sctidRepository.findBySystemIdAndNamespace(systemId, namespaceId))
                .thenReturn(List.of(mockSctid));

        Sctid result = sctidService.getSctWithSystemId(authenticateResponseDto, namespaceId, systemId);

        assertNotNull(result);
        assertEquals("1234567890", result.getSctid());
    }

    @Test
    void getSctWithSystemId_AuthorizedUser_EmptyList_ReturnsNull() throws CisException {
        Integer namespaceId = 1234;
        String systemId = "system-id-1234";

        when(bulkSctidService.isAbleUser(String.valueOf(namespaceId), authenticateResponseDto)).thenReturn(true);
        when(sctidRepository.findBySystemIdAndNamespace(systemId, namespaceId))
                .thenReturn(List.of()); // empty list

        Sctid result = sctidService.getSctWithSystemId(authenticateResponseDto, namespaceId, systemId);

        assertNull(result);
    }


    @Test
    void getSctWithSystemId_UnauthorizedUser_ThrowsException() {
        Integer namespaceId = 1234;
        String systemId = "system-id-1234";

        when(bulkSctidService.isAbleUser(String.valueOf(namespaceId), authenticateResponseDto)).thenReturn(false);

        CisException exception = assertThrows(CisException.class, () ->
                sctidService.getSctWithSystemId(authenticateResponseDto, namespaceId, systemId)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("No permission for the selected operation", exception.getMessage());
    }

    // deprecate sct tests - start

    @Test
    void deprecateSct_WithAuthorizedUserAndValidStatus_UpdatesAndReturnsSctid() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;

        String comment = "Deprecate reason";
        String oldStatus = "Assigned";
        String newStatus = "Deprecate";

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "test-software", "Release comment");

        Sctid existingSct = createTestSctid(sctid);
        existingSct.setStatus(oldStatus);

        injectStateMachine("deprecate", newStatus);

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");
        when(sctIdHelper.getSctid(sctid)).thenReturn(existingSct);

        Sctid updatedSct = createTestSctid(sctid);
        updatedSct.setStatus("Deprecate");
        updatedSct.setComment(comment);

        when(sctidRepository.save(any(Sctid.class))).thenReturn(updatedSct);

        Sctid result = sctidService.deprecateSct(authenticateResponseDto, request);

        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(comment, result.getComment());
    }

    @Test
    void deprecateSct_NamespaceMismatch_ThrowsBadRequestException() {
        String sctid = "12345678901";
        Integer requestNamespace = 1234;
        Integer actualNamespace = 5678;

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, requestNamespace, "test-mismatch", "Mismatch test");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(actualNamespace);

        CisException ex = assertThrows(CisException.class,
                () -> sctidService.deprecateSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Namespaces differences between sctId and parameter", ex.getMessage());
    }

    @Test
    void deprecateSct_UnauthorizedUser_ThrowsUnauthorizedException(){
        String sctid = "12345678901";
        Integer namespace = 1234;

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "test-unauthorized", "Unauthorized access");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(false);

        CisException ex = assertThrows(CisException.class,
                () -> sctidService.deprecateSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getMessage());
    }

    @Test
    void deprecateSct_SctidNotFound_ThrowsAcceptedException() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "test-not-found", "Missing record");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");

        Sctid emptySct = new Sctid();
        emptySct.setSctid("");
        when(sctIdHelper.getSctid(sctid)).thenReturn(emptySct);

        CisException ex = assertThrows(CisException.class,
                () -> sctidService.deprecateSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.ACCEPTED, ex.getStatus());
        assertEquals("No Sctid Record Found", ex.getMessage());
    }

    @Test
    void deprecateSct_InvalidStatusTransition_ThrowsBadRequestException() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;
        String oldStatus = "Available";

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "test-invalid-status", "invalid-status");

        Sctid existingSct = createTestSctid(sctid);
        existingSct.setStatus(oldStatus);

        injectStateMachine("deprecate", "Deprecate");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");
        when(sctIdHelper.getSctid(sctid)).thenReturn(existingSct);

        StateMachine stateMachineSet = spy(stateMachine);
        doReturn(null).when(stateMachineSet).getNewStatus(oldStatus, "Deprecate");
        ReflectionTestUtils.setField(sctidService, "stateMachine", stateMachineSet);

        CisException ex = assertThrows(CisException.class,
                () -> sctidService.deprecateSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Cannot deprecate SCTID:" + sctid + ", current status: " + oldStatus, ex.getMessage());
    }

    // deprecate sct tests - end


    // release sct tests - start

    @Test
    void releaseSct_WithAuthorizedUserAndValidStatus_UpdatesAndReturnsSctid() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;
        String software = "test-software";
        String comment = "Release comment";
        String oldStatus = "Assigned";
        String newStatus = "Available";

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, software, comment);

        injectStateMachine("release", "Release");

        Sctid existingSct = createTestSctid(sctid);
        existingSct.setStatus(oldStatus);

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");
        when(sctIdHelper.getSctid(sctid)).thenReturn(existingSct);

        Sctid updatedSct = createTestSctid(sctid);
        updatedSct.setStatus(newStatus);
        updatedSct.setComment(comment);

        when(sctidRepository.save(any(Sctid.class))).thenReturn(updatedSct);

        Sctid result = sctidService.releaseSct(authenticateResponseDto, request);

        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(comment, result.getComment());
    }

    @Test
    void releaseSct_NamespaceMismatch_ThrowsCisException() {
        String sctid = "12345678901";
        Integer expectedNamespace = 1234;
        Integer mismatchedNamespace = 4321;

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, mismatchedNamespace, "release-namespace-mismatch", "test-release-mismatch");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(expectedNamespace);

        CisException exception = assertThrows(CisException.class,
                () -> sctidService.releaseSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.ACCEPTED, exception.getStatus());
        assertEquals("Namespaces differences between sctId and parameter", exception.getMessage());
    }

    @Test
    void releaseSct_UnauthorizedUser_ThrowsUnauthorizedException() {
        String sctid = "12345678901";
        Integer namespace = 1234;

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "release-unauthorized", "test-release-unauthorized");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(false);

        CisException exception = assertThrows(CisException.class,
                () -> sctidService.releaseSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("No permission for the selected operation", exception.getMessage());
    }

    @Test
    void releaseSct_SctidNotFound_ThrowsCisException() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "release-sctid-notfound", "test-release-notfound");

        Sctid emptySctid = new Sctid();
        emptySctid.setSctid("");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");
        when(sctIdHelper.getSctid(sctid)).thenReturn(emptySctid);

        CisException exception = assertThrows(CisException.class,
                () -> sctidService.releaseSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.ACCEPTED, exception.getStatus());
        assertEquals("No Sctid Record Found", exception.getMessage());
    }

    @Test
    void releaseSct_InvalidStatusTransition_ThrowsBadRequestException() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;
        String oldStatus = "available";

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "release-test-invalid-status", "test-invalid-status");

        injectStateMachine("release", "Release");

        Sctid sct = createTestSctid(sctid);
        sct.setStatus(oldStatus);

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");
        when(sctIdHelper.getSctid(sctid)).thenReturn(sct);

        CisException exception = assertThrows(CisException.class,
                () -> sctidService.releaseSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Cannot release SCTID"));
    }

    // release sct tests - end

    // publish sct tests - start

    @Test
    void publishSct_WithAuthorizedUserAndValidStatus_UpdatesAndReturnsSctid() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;
        String oldStatus = "Assigned";
        String newStatus = "Published";

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "publish-sctid", "test-publish-sctid");

        injectStateMachine("publish", "Publish");

        Sctid existingSct = createTestSctid(sctid);
        existingSct.setStatus(oldStatus);

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");
        when(sctIdHelper.getSctid(sctid)).thenReturn(existingSct);

        Sctid updatedSct = createTestSctid(sctid);
        updatedSct.setStatus(newStatus);
        updatedSct.setComment("Publishing reason");

        when(sctidRepository.save(any(Sctid.class))).thenReturn(updatedSct);

        Sctid result = sctidService.publishSct(authenticateResponseDto, request);

        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals("Publishing reason", result.getComment());
    }

    @Test
    void publishSct_NamespaceMismatch_ThrowsAcceptedException() {
        String sctid = "12345678901";
        int expectedNamespace = 9999;
        int mismatchedNamespace = 1234;

        when(sctIdHelper.getNamespace(sctid)).thenReturn(expectedNamespace);

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, mismatchedNamespace, "publish-mismatch", "test-publish-mismatch");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(expectedNamespace);

        CisException ex = assertThrows(CisException.class,
                () -> sctidService.publishSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.ACCEPTED, ex.getStatus());
        assertEquals("Namespaces differences between sctId and parameter", ex.getMessage());
    }

    @Test
    void publishSct_UnauthorizedUser_ThrowsUnauthorizedException() {
        String sctid = "12345678901";
        int namespace = 1234;

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "publish-unauthorized", "test-publish-unauthorized");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(false);

        CisException ex = assertThrows(CisException.class,
                () -> sctidService.publishSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("No permission for the selected operation", ex.getMessage());
    }

    @Test
    void publishSct_SctidNotFound_ThrowsAcceptedException() throws CisException {
        String sctid = "12345678901";
        int namespace = 1234;

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "publish-not-found", "test-publish-not-found");

        Sctid emptySct = new Sctid();
        emptySct.setSctid("");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");
        when(sctIdHelper.getSctid(sctid)).thenReturn(emptySct);

        CisException ex = assertThrows(CisException.class,
                () -> sctidService.publishSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.ACCEPTED, ex.getStatus());
        assertEquals("No Sctid Rec Found", ex.getMessage());
    }

    @Test
    void publishSct_InvalidStatusTransition_ThrowsBadRequestException() throws CisException {
        String sctid = "12345678901";
        int namespace = 1234;
        String oldStatus = "Published";

        DeprecateSctRequestDTO request = createDeprecateRequest(sctid, namespace, "publish-invalid-transition", "test-publish-invalid-transition");

        Sctid existingSct = createTestSctid(sctid);
        existingSct.setStatus(oldStatus);

        injectStateMachine("publish", "Publish");

        StateMachine stateMachineSpy = spy(stateMachine);
        doReturn(null).when(stateMachineSpy).getNewStatus(oldStatus, "Publish");
        ReflectionTestUtils.setField(sctidService, "stateMachine", stateMachineSpy);

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn("test-author");
        when(sctIdHelper.getSctid(sctid)).thenReturn(existingSct);

        CisException ex = assertThrows(CisException.class,
                () -> sctidService.publishSct(authenticateResponseDto, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Cannot publish SCTID:" + sctid + ", current status: " + oldStatus, ex.getMessage());
    }

    // publish sct tests - end

    // register sct tests - start

    private SCTIDRegistrationRequest buildRequest(String sctid, Integer namespace, String systemId, String software, String comment) {
        SCTIDRegistrationRequest request = new SCTIDRegistrationRequest();
        request.setSctid(sctid);
        request.setNamespace(namespace);
        request.setSystemId(systemId);
        request.setSoftware(software);
        request.setComment(comment);
        return request;
    }


    @Test
    void registerSctid_WithAutoSystemIdAndAuthorizedUser_ReturnsSavedSctid() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;
        String software = "test-software";
        String comment = "auto sys id";
        String author = "test-author";

        SCTIDRegistrationRequest request = buildRequest(sctid, namespace, "", software, comment);

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(namespace.toString(), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn(author);

        Sctid savedSctid = createTestSctid(sctid);
        savedSctid.setNamespace(namespace);

        when(sctIdDM.registerSctid(any(SCTIDRegisterRequest.class), eq("SCTIDRegisterRequest"))).thenReturn(savedSctid);

        Sctid result = sctidService.registerSctid(authenticateResponseDto, request);

        assertNotNull(result);
        assertEquals(sctid, result.getSctid());
        assertEquals(namespace, result.getNamespace());
    }

    @Test
    void registerSctid_WithExistingSystemIdAndAssignedStatus_ReturnsExistingSctid() throws CisException {
        String sctid = "12345678901";
        Integer namespace = 1234;
        String systemId = "SYS123";
        String software = "test-software";
        String comment = "Registering existing";

        SCTIDRegistrationRequest request = buildRequest(sctid, namespace, systemId, software, comment);

        Sctid existingSctid = createTestSctid(sctid);
        existingSctid.setSystemId(systemId);
        existingSctid.setNamespace(namespace);
        existingSctid.setStatus("Assigned");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(namespace.toString(), authenticateResponseDto)).thenReturn(true);
        doReturn(existingSctid)
                .when(sctIdDM)
                .registerSctid(any(SCTIDRegisterRequest.class), eq("SCTIDRegisterRequest"));

        Sctid result = sctidService.registerSctid(authenticateResponseDto, request);

        assertNotNull(result);
        assertEquals(sctid, result.getSctid());
        assertEquals(systemId, result.getSystemId());
        assertEquals("Assigned", result.getStatus());
    }


    @Test
    void registerSctid_UnauthorizedUser_ThrowsException() {
        String sctid = "12345678901";
        Integer namespace = 1234;

        SCTIDRegistrationRequest request = buildRequest(sctid, namespace, "sys-1", "unauthorized-test", "unauthorized-test");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(namespace);
        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(false);

        CisException exception = assertThrows(CisException.class,
                () -> sctidService.registerSctid(authenticateResponseDto, request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("No permission for the selected operation", exception.getMessage());
    }

    @Test
    void registerSctid_WithNamespaceMismatch_ThrowsException() {
        String sctid = "12345678901";
        Integer actualNamespace = 1234;
        Integer requestNamespace = 5678; // mismatched namespace

        SCTIDRegistrationRequest request = buildRequest(sctid, requestNamespace, "sys-1-23", "namespace-mismatch-test", "namespace-mismatch-test");

        when(sctIdHelper.getNamespace(sctid)).thenReturn(actualNamespace);

        CisException exception = assertThrows(CisException.class,
                () -> sctidService.registerSctid(authenticateResponseDto, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Namespaces differences between sctId and parameter", exception.getMessage());
    }

    // register sct tests - end


    // reserve sct tests - start

    public static SCTIDReservationRequest buildReservationRequest(
            Integer namespace, String partitionId, String software, String comment, String expirationDate) {

        SCTIDReservationRequest request = new SCTIDReservationRequest();
        request.setNamespace(namespace);
        request.setPartitionId(partitionId);
        request.setSoftware(software);
        request.setComment(comment);
        request.setExpirationDate(expirationDate);
        return request;
    }

    @Test
    void reserveSctid_WithValidRequestAndAvailableRecord_ReturnsReservedSctid() throws CisException {
        Integer namespace = 1234;
        String partitionId = "10";
        String status = "Reserved";
        String sctid = "12345678901";
        String author = "test-author";

        SctidService sctidServiceSpy = Mockito.spy(sctidService);

        SCTIDReservationRequest reservationRequest = buildReservationRequest(
                namespace, partitionId, "test-software", "comment", String.valueOf(LocalDateTime.now().plusDays(5))
        );

        Sctid reservedSctid = createTestSctid(sctid);
        reservedSctid.setStatus(status);
        reservedSctid.setNamespace(namespace);

        when(bulkSctidService.isAbleUser(namespace.toString(), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn(author);
        doReturn(reservedSctid).when(sctidServiceSpy)
                .reserveSctid(any(SCTIDReserveRequest.class));

        Sctid result = sctidServiceSpy.reserveSctid(authenticateResponseDto, reservationRequest);

        assertNotNull(result);
        assertEquals(sctid, result.getSctid());
        assertEquals(status, result.getStatus());
    }

    @Test
    void reserveSctid_UnauthorizedUser_ThrowsException() {
        Integer namespace = 1234;
        String partitionId = "10";

        SCTIDReservationRequest reservationRequest = buildReservationRequest(
                namespace, partitionId, "software", "unauthorized", String.valueOf(LocalDateTime.now().plusDays(1))
        );

        when(bulkSctidService.isAbleUser(namespace.toString(), authenticateResponseDto)).thenReturn(false);

        CisException exception = assertThrows(CisException.class,
                () -> sctidService.reserveSctid(authenticateResponseDto, reservationRequest));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("No permission for the selected operation", exception.getMessage());
    }

    @Test
    void reserveSctid_WithNamespacePartitionMismatch_ThrowsException() {
        Integer namespace = 0;
        String invalidPartitionId = "10";

        SCTIDReservationRequest reservationRequest = buildReservationRequest(
                namespace, invalidPartitionId, "software", "mismatch", String.valueOf(LocalDateTime.now().plusDays(1))
        );

        when(bulkSctidService.isAbleUser(String.valueOf(namespace), authenticateResponseDto)).thenReturn(true);

        CisException exception = assertThrows(CisException.class,
                () -> sctidService.reserveSctid(authenticateResponseDto, reservationRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Namespace and partitionId parameters are not consistent.", exception.getMessage());
    }

    @Test
    void reserveSctid_InternalReserveReturnsNull_ReturnsNull() throws CisException {
        Integer namespace = 1234;
        String partitionId = "10";
        String author = "test-author";

        SctidService sctidServiceSpy = Mockito.spy(sctidService);

        SCTIDReservationRequest reservationRequest = buildReservationRequest(
                namespace, partitionId, "software", "test", String.valueOf(LocalDateTime.now().plusDays(2))
        );

        when(bulkSctidService.isAbleUser(namespace.toString(), authenticateResponseDto)).thenReturn(true);
        when(authenticateResponseDto.getName()).thenReturn(author);

        doReturn(null).when(sctidServiceSpy).reserveSctid(any(SCTIDReserveRequest.class));

        Sctid result = sctidServiceSpy.reserveSctid(authenticateResponseDto, reservationRequest);

        assertNull(result);
    }

    @Test
    void setAvailableSCTIDRecord2NewStatus_RecordAvailable_UpdatesStatus() throws CisException {
        String action = "reserve";
        String currentStatus = "Available";
        String newStatus = "Reserved";
        Integer namespace = 1234;
        String partitionId = "10";

        Map<String, String> statuses = new HashMap<>();
        statuses.put("available", currentStatus);

        stateMachine.statuses = statuses;

        SCTIDReserveRequest request = new SCTIDReserveRequest();
        request.setNamespace(namespace);
        request.setPartitionId(partitionId);

        Sctid existingSctid = new Sctid();
        existingSctid.setStatus(currentStatus);

        List<Sctid> availableList = List.of(existingSctid);

        SctidService spyService = Mockito.spy(sctidService);
        doReturn(availableList).when(spyService).findSctWithIndexAndLimit(anyMap(), any(), isNull());

        when(stateMachine.getNewStatus(currentStatus, action)).thenReturn(newStatus);

        Sctid updated = new Sctid();
        updated.setStatus(newStatus);
        when(sctidRepository.save(any(Sctid.class))).thenReturn(updated);

        Sctid result = spyService.setAvailableSCTIDRecord2NewStatus(request, action);

        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(sctidRepository).save(existingSctid);
    }

    // reserve sct tests - end




}
