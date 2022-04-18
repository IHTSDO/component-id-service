package com.snomed.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snomed.api.controller.dto.BulkJobsListResponse;
import com.snomed.api.controller.dto.CleanUpServiceResponse;
import com.snomed.api.domain.BulkJob;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.BulkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BulkJobsController {
    @Autowired
    BulkJobService bulkJobService;

    @GetMapping("/bulk/jobs")
    @ResponseBody
    public List<BulkJobsListResponse> getJobs(@RequestParam  String token) throws APIException {
        return bulkJobService.getJobs(token);
    }

    @GetMapping("/bulk/jobs/{jobId}")
    @ResponseBody
    public BulkJob getJob(@RequestParam String token, @PathVariable Integer jobId) throws APIException {
        return bulkJobService.getJob(token,jobId);
    }

    @GetMapping("/bulk/jobs/{jobId}/records")
    @ResponseBody
    public List<Object> getJobRecords(@RequestParam String token,@PathVariable Integer jobId) throws APIException, JsonProcessingException {
        return bulkJobService.getJobRecords(token,jobId);
    }

    @GetMapping("/bulk/jobs/cleanupExpired")
    @ResponseBody
    public List<CleanUpServiceResponse> cleanUpExpiredIds(@RequestParam String token) throws APIException {
        return bulkJobService.cleanUpExpiredIds(token);
    }
}
