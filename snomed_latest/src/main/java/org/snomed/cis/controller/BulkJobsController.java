package org.snomed.cis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.dto.CleanUpServiceResponse;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.BulkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "Bulk Jobs", value = "Bulk Jobs")
@RestController
public class BulkJobsController {

    private final Logger logger = LoggerFactory.getLogger(BulkJobsController.class);

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
    public List<BulkJob> getJobs(@RequestParam String token) throws CisException {
        logger.info("Request received - token :: {}", token);
        return bulkJobService.getJobs(token);
    }

    @GetMapping("/bulk/jobs/{jobId}")
    @ResponseBody
    public BulkJob getJob(@RequestParam String token, @PathVariable Integer jobId) throws CisException {
        return bulkJobService.getJob(token,jobId);
    }

    @GetMapping("/bulk/jobs/{jobId}/records")
    @ResponseBody
    public List<Object> getJobRecords(@RequestParam String token,@PathVariable Integer jobId) throws CisException, JsonProcessingException {
        return bulkJobService.getJobRecords(token,jobId);
    }

    @GetMapping("/bulk/jobs/cleanupExpired")
    @ResponseBody
    public List<CleanUpServiceResponse> cleanUpExpiredIds(@RequestParam String token) throws CisException {
        return bulkJobService.cleanUpExpiredIds(token);
    }
}
