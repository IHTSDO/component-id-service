package org.snomed.cis.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.dto.CleanUpServiceResponse;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.AuthorizationService;
import org.snomed.cis.service.BulkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BulkJobsController.class)
class BulkJobsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BulkJobService bulkJobService;

    @MockBean
    private AuthorizationService authorizationService;

    @Test
    void testGetJobs_shouldReturnListOfJobs() throws Exception {
        BulkJob job1 = createBulkJob(1, "Job 1");
        BulkJob job2 = createBulkJob(2, "Job 2");

        List<BulkJob> mockJobs = List.of(job1, job2);

        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJobs()).thenReturn(mockJobs);

        mockMvc.perform(get("/bulk/jobs")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    private BulkJob createBulkJob(int id, String name) throws Exception {
        BulkJob job = new BulkJob();

        Field idField = BulkJob.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(job, id);

        Field nameField = BulkJob.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(job, name);

        return job;
    }

    @Test
    void testGetJobs_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJobs()).thenThrow(new RuntimeException("Failed to fetch jobs"));

        mockMvc.perform(get("/bulk/jobs")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Failed to fetch jobs"));
    }

    @Test
    @WithMockUser
    void testGetJobs_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bulk/jobs")) // no token param
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testGetJobs_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/bulk/jobs").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetJobs_emptyList_shouldReturnEmptyArray() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJobs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/bulk/jobs")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    void testGetJob_success_shouldReturnJob() throws Exception {
        BulkJob mockJob = new BulkJob();
        mockJob.setId(1);
        mockJob.setName("Sample Job");

        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJob(1)).thenReturn(mockJob);

        mockMvc.perform(get("/bulk/jobs/1")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sample Job"));
    }


    @Test
    void testGetJob_notFound_shouldReturnNotFound() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJob(999)).thenThrow(new CisException(HttpStatus.NOT_FOUND, "Job not found"));

        mockMvc.perform(get("/bulk/jobs/999")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Job not found"));
    }


    @Test
    @WithMockUser
    void testGetJob_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bulk/jobs/1")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void testGetJob_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/bulk/jobs/1").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetJob_serviceThrows_shouldReturnInternalServerError() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJob(1)).thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/bulk/jobs/1")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Unexpected failure"));
    }


    @Test
    void testGetJobRecords_success_shouldReturnList() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        List<Object> records = List.of("Record1", "Record2");

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJobRecords(1)).thenReturn(records);

        mockMvc.perform(get("/bulk/jobs/1/records")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Record1"))
                .andExpect(jsonPath("$[1]").value("Record2"));
    }


    @Test
    void testGetJobRecords_empty_shouldReturnEmptyList() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJobRecords(99)).thenReturn(List.of());

        mockMvc.perform(get("/bulk/jobs/99/records")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    void testGetJobRecords_serviceThrows_shouldReturnInternalServerError() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.doNothing().when(authorizationService).validateAdmin(Mockito.any());
        Mockito.when(bulkJobService.getJobRecords(1)).thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/bulk/jobs/1/records")
                        .param("token", "dummy-token")
                        .with(authentication(authToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Unexpected failure"));
    }

    @Test
    @WithMockUser
    void testGetJobRecords_missingToken_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bulk/jobs/1/records")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void testGetJobRecords_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/bulk/jobs/1/records").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testCleanUpExpiredIds_success_shouldReturnList() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        CleanUpServiceResponse response1 = new CleanUpServiceResponse();
        response1.setMessage("Deleted");
        response1.setModel("ID123");
        response1.setAffectedRows(1);

        CleanUpServiceResponse response2 = new CleanUpServiceResponse();
        response2.setMessage("Skipped");
        response2.setModel("ID456");
        response2.setAffectedRows(0);

        List<CleanUpServiceResponse> responseList = List.of(response1, response2);

        Mockito.when(bulkJobService.cleanUpExpiredIds(mockDto)).thenReturn(responseList);

        mockMvc.perform(get("/bulk/jobs/cleanupExpired").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].message").value("Deleted")).andExpect(jsonPath("$[0].model").value("ID123")).andExpect(jsonPath("$[0].affectedRows").value(1)).andExpect(jsonPath("$[1].message").value("Skipped")).andExpect(jsonPath("$[1].model").value("ID456")).andExpect(jsonPath("$[1].affectedRows").value(0));
    }

    @Test
    @WithMockUser
    void testCleanUpExpiredIds_cisException_shouldReturnError() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());


        Mockito.when(bulkJobService.cleanUpExpiredIds(mockDto)).thenThrow(new CisException(HttpStatus.BAD_REQUEST, "Invalid authentication"));

        mockMvc.perform(get("/bulk/jobs/cleanupExpired").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400)).andExpect(jsonPath("$.message").value("Invalid authentication"));
    }

    @Test
    void testCleanUpExpiredIds_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/bulk/jobs/cleanupExpired").param("token", "dummy-token")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testCleanUpExpiredIds_missingToken_shouldReturnBadRequest() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        mockMvc.perform(get("/bulk/jobs/cleanupExpired").with(authentication(authToken))).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testCleanUpExpiredIds_runtimeException_shouldReturnInternalServerError() throws Exception {
        AuthenticateResponseDto mockDto = Mockito.mock(AuthenticateResponseDto.class);
        Authentication authToken = new AuthorizationControllerMockMvcTest.TestToken("dummy-token", "admin", mockDto, List.of());

        Mockito.when(bulkJobService.cleanUpExpiredIds(mockDto)).thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/bulk/jobs/cleanupExpired").param("token", "dummy-token").with(authentication(authToken))).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.message").value("java.lang.RuntimeException: Unexpected failure"));
    }

}
