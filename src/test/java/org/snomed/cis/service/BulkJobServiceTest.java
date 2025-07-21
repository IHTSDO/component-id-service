package org.snomed.cis.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.CleanUpServiceResponse;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.BulkJobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkJobServiceTest {

    @Spy
    @InjectMocks
    private BulkJobService bulkJobService;

    @Mock
    private BulkJobRepository bulkJobRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private AuthenticateResponseDto authToken;

    @Test
    void testGetJobs() {
        BulkJob job1 = new BulkJob();
        job1.setId(1);
        job1.setName("Job 1");
        job1.setStatus("Completed");
        job1.setCreated_at(LocalDateTime.now());

        BulkJob job2 = new BulkJob();
        job2.setId(2);
        job2.setName("Job 2");
        job2.setStatus("In Progress");
        job2.setCreated_at(LocalDateTime.now());

        List<BulkJob> mockJobs = Arrays.asList(job1, job2);

        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString(), eq(BulkJob.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(mockJobs);

        List<BulkJob> jobs = bulkJobService.getJobs();

        Assertions.assertNotNull(jobs);
        Assertions.assertEquals(2, jobs.size());
        Assertions.assertEquals("Job 1", jobs.get(0).getName());
        Assertions.assertEquals("Job 2", jobs.get(1).getName());
    }

    @Test
    void testGetJobSuccess() throws CisException {
        BulkJob job = new BulkJob();
        job.setId(1);
        job.setName("Test Job");
        job.setStatus("Completed");

        when(bulkJobRepository.findById(1)).thenReturn(Optional.of(job));

        BulkJob result = bulkJobService.getJob(1);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getId());
        Assertions.assertEquals("Test Job", result.getName());
        Assertions.assertEquals("Completed", result.getStatus());
    }

    @Test
    void testGetJobNotFoundThrowsException() {
        int jobId = 99;
        when(bulkJobRepository.findById(jobId)).thenReturn(Optional.empty());

        CisException exception = Assertions.assertThrows(CisException.class, () -> {
            bulkJobService.getJob(jobId);
        });

        Assertions.assertEquals("There is no result from Database for jobId" + jobId, exception.getMessage());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testGetJobRecordsReturnsSchemeIds() {
        int jobId = 1;

        BulkJob mockJob = new BulkJob();
        mockJob.setId(jobId);
        mockJob.setRequest("{\"model\":\"SchemeId\"}");

        SchemeId scheme1 = new SchemeId();
        SchemeId scheme2 = new SchemeId();
        List<SchemeId> schemeList = Arrays.asList(scheme1, scheme2);

        when(bulkJobRepository.findById(jobId)).thenReturn(Optional.of(mockJob));
        doReturn(schemeList).when(bulkJobService).findSchemeByJobId(jobId);

        List<Object> result = bulkJobService.getJobRecords(jobId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertInstanceOf(SchemeId.class, result.get(0));
    }

    @Test
    void testGetJobRecordsReturnsSctids() {
        int jobId = 2;

        BulkJob mockJob = new BulkJob();
        mockJob.setId(jobId);
        mockJob.setRequest("{\"model\":\"Sctid\"}");

        Sctid sctid1 = new Sctid();
        Sctid sctid2 = new Sctid();
        List<Sctid> sctidList = Arrays.asList(sctid1, sctid2);

        when(bulkJobRepository.findById(jobId)).thenReturn(Optional.of(mockJob));
        doReturn(sctidList).when(bulkJobService).findSctidByJobId(jobId);

        List<Object> result = bulkJobService.getJobRecords(jobId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertInstanceOf(Sctid.class, result.get(0));
    }

    @Test
    void testGetJobRecordsWhenJobNotFoundReturnsNull() {
        int jobId = 404;
        when(bulkJobRepository.findById(jobId)).thenReturn(Optional.empty());

        List<Object> result = bulkJobService.getJobRecords(jobId);

        Assertions.assertNull(result);
    }

    @Test
    void testFindSchemeByJobId_UniqueSchemeids() {
        int jobId = 123;

        SchemeId scheme1 = new SchemeId();
        scheme1.setSystemId("SYS001");

        SchemeId scheme2 = new SchemeId();
        scheme2.setSystemId("SYS002");

        List<SchemeId> mockResultList = List.of(scheme1, scheme2);

        Query mockQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString(), eq(SchemeId.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockResultList);

        List<SchemeId> result = bulkJobService.findSchemeByJobId(jobId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }

    @Test
    void testFindSctidByJobId_UniqueSctids() {
        Integer jobId = 101;

        Sctid sctid1 = new Sctid();
        sctid1.setSystemId("SCT001");
        sctid1.setSctid("100001");
        Sctid sctid2 = new Sctid();
        sctid2.setSystemId("SCT002");
        sctid2.setSctid("100002");
        Sctid sctid3 = new Sctid();
        sctid3.setSystemId("SCT003");
        sctid3.setSctid("100003");

        List<Sctid> mockQueryResult = Arrays.asList(sctid1, sctid2, sctid3);

        Query mockQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString(), eq(Sctid.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockQueryResult);

        List<Sctid> result = bulkJobService.findSctidByJobId(jobId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals("100001", result.get(0).getSctid());
        Assertions.assertEquals("100002", result.get(1).getSctid());
        Assertions.assertEquals("100003", result.get(2).getSctid());
    }

    @Test
    void testFindFieldSelect_WithFiltersAndOrdering() {
        Map<String, String> queryObject = Map.of("status", "'Completed'");
        Map<String, Integer> fields = Map.of("id", 1, "name", 1);
        Map<String, String> orderBy = Map.of("created_at", "D");

        BulkJob job = new BulkJob();
        job.setId(1);
        job.setName("Sample Job");
        job.setStatus("Completed");
        job.setCreated_at(LocalDateTime.now());

        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString(), eq(BulkJob.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(job));

        List<BulkJob> result = bulkJobService.findFieldSelect(queryObject, fields, 10, 0, orderBy);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Sample Job", result.get(0).getName());

        verify(entityManager).createNativeQuery(contains("WHERE status='Completed'"), eq(BulkJob.class));
        verify(entityManager).createNativeQuery(contains("order by created_at desc"), eq(BulkJob.class));
        verify(entityManager).createNativeQuery(contains("limit 10"), eq(BulkJob.class));
    }

    @Test
    void testFindFieldSelect_WithLimitAndSkip() {
        Map<String, String> queryObject = new HashMap<>();
        Map<String, Integer> fields = Map.of("id", 1, "name", 1);
        Map<String, String> orderBy = Map.of("id", "A");

        BulkJob job1 = new BulkJob();
        job1.setId(1);
        job1.setName("Job 1");

        BulkJob job2 = new BulkJob();
        job2.setId(2);
        job2.setName("Job 2");

        BulkJob job3 = new BulkJob();
        job3.setId(3);
        job3.setName("Job 3");

        List<BulkJob> allJobs = List.of(job1, job2, job3);

        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString(), eq(BulkJob.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(allJobs);

        List<BulkJob> result = bulkJobService.findFieldSelect(queryObject, fields, 2, 1, orderBy);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Job 2", result.get(0).getName());
        Assertions.assertEquals("Job 3", result.get(1).getName());
    }

    @Test
    void testFindFieldSelect_NullFieldsThrowsNPE() {
        Map<String, String> queryObject = new HashMap<>();
        Map<String, Integer> fields = null;
        Map<String, String> orderBy = Map.of("id", "A");
        Integer limit = 10;
        Integer skip = 0;

        Assertions.assertThrows(NullPointerException.class, () -> {
            bulkJobService.findFieldSelect(queryObject, fields, limit, skip, orderBy);
        }, "Calling size() on a null map should throw NullPointerException.");
    }

    @Test
    void testCleanUpExpiredIdsAsAdmin() throws CisException {
        authToken.setRoles(List.of("ROLE_component-identifier-service-admin"));

        BulkJobService realService = new BulkJobService();
        ReflectionTestUtils.setField(realService, "bulkJobRepository", bulkJobRepository);

        BulkJobService spyService = Mockito.spy(realService);

        Mockito.doReturn(true).when(spyService).isAbleUser(authToken);

        when(bulkJobRepository.cleanExpiredSctids()).thenReturn(5);
        when(bulkJobRepository.cleanExpiredSchemeids()).thenReturn(3);

        List<CleanUpServiceResponse> result = spyService.cleanUpExpiredIds(authToken);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        Assertions.assertEquals("SctId", result.get(0).getModel());
        Assertions.assertEquals(5, result.get(0).getAffectedRows());

        Assertions.assertEquals("SchemeId", result.get(1).getModel());
        Assertions.assertEquals(3, result.get(1).getAffectedRows());
    }

    @Test
    void testCleanUpExpiredIdsAsUnauthorizedUser() throws CisException {

        BulkJobService realService = new BulkJobService();
        ReflectionTestUtils.setField(realService, "bulkJobRepository", bulkJobRepository);

        BulkJobService spyService = Mockito.spy(realService);
        Mockito.doReturn(false).when(spyService).isAbleUser(authToken);

        CisException thrown = Assertions.assertThrows(CisException.class, () -> {
            spyService.cleanUpExpiredIds(authToken);
        });

        Assertions.assertEquals("No permission for the selected operation", thrown.getMessage());
    }

    @Test
    void testCleanUpExpiredIdsWhenNoJobsToClean() throws CisException {

        doReturn(true).when(bulkJobService).isAbleUser(authToken);

        when(bulkJobRepository.cleanExpiredSctids()).thenReturn(0);
        when(bulkJobRepository.cleanExpiredSchemeids()).thenReturn(0);

        List<CleanUpServiceResponse> result = bulkJobService.cleanUpExpiredIds(authToken);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("SctId", result.get(0).getModel());
        Assertions.assertEquals("SchemeId", result.get(1).getModel());
        Assertions.assertEquals(0L, result.get(0).getAffectedRows().longValue());
        Assertions.assertEquals(0L, result.get(1).getAffectedRows().longValue());
    }

    @Test
    void testIsAbleUser_AdminRolePresent() throws CisException {
        when(authToken.getRoles()).thenReturn(List.of("ROLE_component-identifier-service-admin", "OTHER_some-user-role"));
        boolean result = bulkJobService.isAbleUser(authToken);
        Assertions.assertTrue(result, "User with admin role should be able.");
    }

    @Test
    void testIsAbleUser_AdminRoleNotPresent() throws CisException {
        when(authToken.getRoles()).thenReturn(List.of("ROLE_NOT_A_ADMIN"));
        boolean result = bulkJobService.isAbleUser(authToken);
        Assertions.assertFalse(result, "User without admin role should not be able.");
    }

}