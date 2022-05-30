package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.dto.CleanUpServiceResponse;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.BulkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    public List<BulkJob> getJobs() throws CisException {
        logger.info("Request received - getJobs");
        return bulkJobService.getJobs();
    }

    @GetMapping("/bulk/jobs/{jobId}")
    @ResponseBody
    public BulkJob getJob( @PathVariable Integer jobId) throws CisException {
        logger.info("Request received - jobId :: {}", jobId);
        return bulkJobService.getJob(jobId);
    }

    @GetMapping("/bulk/jobs/{jobId}/records")
    @ResponseBody
    public List<Object> getJobRecords(@PathVariable Integer jobId) {
        logger.info("Request received for - jobId :: {}", jobId);
        return bulkJobService.getJobRecords(jobId);
    }

    @GetMapping("/bulk/jobs/cleanupExpired")
    @ResponseBody
    public List<CleanUpServiceResponse> cleanUpExpiredIds( Authentication authentication) throws CisException {
        logger.info("Request received - authentication :: {}", authentication);
        Token authToken = (Token)authentication;
        return bulkJobService.cleanUpExpiredIds(authToken.getAuthenticateResponseDto());
    }
}
