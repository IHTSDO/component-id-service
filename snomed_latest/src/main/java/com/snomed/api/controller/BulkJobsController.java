package com.snomed.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snomed.api.controller.dto.BulkJobsListResponse;
import com.snomed.api.controller.dto.CleanUpServiceResponse;
import com.snomed.api.domain.BulkJob;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.BulkJobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "Bulk Jobs", value = "Bulk Jobs")
@RestController
@RequestMapping(path = "/api")
public class BulkJobsController {
    @Autowired
    BulkJobService bulkJobService;

    @ApiOperation(
            value="Bulk Job Service",
            notes="Returns a list bulk jobs"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; bullJobService &ndash; the list of descendants of the concept<br>",tags = { "Bulk Job Operations" })
    @ApiResponses({
           // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
           // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
           // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })
    @GetMapping("/bulk/jobs")
    @ResponseBody
    public List<BulkJob> getJobs(@RequestParam String token) throws APIException {
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
