package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.dto.CleanUpServiceResponse;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.BulkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
@Tag(name = "Bulk Jobs" , description = "Bulk Jobs Controller")
@RestController
public class BulkJobsController {

    private final Logger logger = LoggerFactory.getLogger(BulkJobsController.class);

    @Autowired
    BulkJobService bulkJobService;

    @Operation(
            summary="Bulk Job Service",
            description="Returns a list bulk jobs"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; bullJobService &ndash; the list of descendants of the concept<br>",tags = { "Bulk Job Operations" })
    @ApiResponses({
           // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
           // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
           // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })
    @GetMapping("/bulk/jobs")
    public ResponseEntity<List<BulkJob>> getJobs(@RequestParam String token) throws CisException {
        logger.info("Request received - getJobs");
        return ResponseEntity.ok(bulkJobService.getJobs());
    }

    @GetMapping("/bulk/jobs/{jobId}")
    public ResponseEntity<BulkJob> getJob(@RequestParam String token, @PathVariable Integer jobId) throws CisException {
        logger.info("Request received - jobId :: {}", jobId);
        return ResponseEntity.ok(bulkJobService.getJob(jobId));
    }

    @GetMapping("/bulk/jobs/{jobId}/records")
    public ResponseEntity<List<Object>> getJobRecords(@RequestParam String token, @PathVariable Integer jobId) {
        logger.info("Request received for - jobId :: {}", jobId);
        return ResponseEntity.ok(bulkJobService.getJobRecords(jobId));
    }

    @GetMapping("/bulk/jobs/cleanupExpired")
    public ResponseEntity<List<CleanUpServiceResponse>> cleanUpExpiredIds(@RequestParam String token, @Parameter(hidden = true) Authentication authentication) throws CisException {
        logger.info("Request received - authentication :: {}", authentication);
        Token authToken = (Token)authentication;
        return ResponseEntity.ok(bulkJobService.cleanUpExpiredIds(authToken.getAuthenticateResponseDto()));
    }
}
