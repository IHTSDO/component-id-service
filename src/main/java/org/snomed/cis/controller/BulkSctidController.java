package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.BulkSctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Tag(name = "SCTIDS - Bulk Operations" , description = "Bulk Sctid Controller")
@RestController
public class BulkSctidController {
    private final Logger logger = LoggerFactory.getLogger(BulkSctidController.class);

    @Autowired
    private BulkSctidService service;

    @Autowired
    SctidRepository sctidRepository;

    @Autowired
    HttpServletResponse httpResponse;

    @Autowired
    HttpServletRequest httpRequest;

    @Operation(
            summary = "Bulk Sct ID",
            description = "Returns a list Sct ID"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; BulkSctidService &ndash; the list of descendants of the concept<br>", tags = {"Bulk Sct ID"})
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })

    @GetMapping("/sct/bulk/ids")
    public ResponseEntity<List<Sctid>> getSctidsByQL(@RequestParam String token, @RequestParam String sctids) throws CisException {
        logger.info("Request received for - ids :: {}", sctids);
        return ResponseEntity.ok(service.getSctByIds(sctids));
    }

    @Operation(summary = "getSctidsByQLPost")
    @PostMapping("/sct/bulk/ids")
    public ResponseEntity<List<Sctid>> getSctidsByQLPost(@RequestParam String token, @RequestBody SctIdRequest sctids) throws CisException {
        logger.info("Request received for - sctids :: {}", sctids);
        return ResponseEntity.ok(service.postSctByIds(sctids));
    }

    @Operation(summary = "getSctidBySystemIds")
    @GetMapping("sct/namespace/{namespaceId}/systemIds")
    public ResponseEntity<List<Sctid>> getSctidBySystemIds(@RequestParam String token, @PathVariable Integer namespaceId, @RequestParam("systemIds") String systemIdStr) {
        logger.info("Request received for - namespaceId :: {} - systemIdStr :: {}", namespaceId, systemIdStr);
        return ResponseEntity.ok(service.getSctidBySystemIds(systemIdStr, namespaceId));
    }

    @Operation(summary = "registerScts")
    @PostMapping("/sct/bulk/register")
    public ResponseEntity<BulkJob> registerScts(@RequestParam String token, @RequestBody RegistrationDataDTO registrationData, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - RegistrationDataDTO :: {} - AuthenticateResponseDTo :: {}", registrationData, authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(service.registerSctids(authToken.getAuthenticateResponseDto(), registrationData));
    }

    @Operation(summary = "generateSctids")
    @PostMapping("/sct/bulk/generate")
    public ResponseEntity<BulkJobResponseDto> generateSctids(@RequestParam String token, @RequestBody @Valid SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Instant start = Instant.now();
        Token authToken = (Token) authentication;
        logger.info("Request received from user {} - request :: {}",((Token) authentication).getUserName(), sctidBulkGenerationRequestDto);
        BulkJobResponseDto bulkJobResponseDto = service.generateSctids(authToken.getAuthenticateResponseDto(), sctidBulkGenerationRequestDto);
        Instant end = Instant.now();
        logger.info("Job {} for user '{}' completed successfully in {} seconds", bulkJobResponseDto.getId(), authToken.getUserName(), (ChronoUnit.MILLIS.between(start,end)/1000.0));
        return ResponseEntity.ok(bulkJobResponseDto);
    }

    @Operation(summary = "deprecateSctid")
    @PutMapping("/sct/bulk/deprecate")
    public ResponseEntity<BulkJob> deprecateSctid(@RequestParam String token, @RequestBody BulkSctRequestDTO deprecationData, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - BulkSctRequestDTO :: {} - AuthenticateResponseDTo :: {}", deprecationData, authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(service.deprecateSctid(authToken.getAuthenticateResponseDto(), deprecationData));
    }

    @Operation(summary = "publishSctid")
    @PutMapping("/sct/bulk/publish")
    public ResponseEntity<BulkJob> publishSctid(@RequestParam String token, @RequestBody BulkSctRequestDTO publishData, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDBulkPublishRequestDto :: {} - AuthenticateResponseDTo :: {}", publishData, authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(service.publishSctid(authToken.getAuthenticateResponseDto(), publishData));
    }

    @Operation(summary = "releaseSctid")
    @PutMapping("/sct/bulk/release")
    public ResponseEntity<BulkJob> releaseSctid(@RequestParam String token, @RequestBody BulkSctRequestDTO publishData, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDBulkReleaseRequestDto :: {} - AuthenticateResponseDTo :: {}", publishData, authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(service.releaseSctid(authToken.getAuthenticateResponseDto(), publishData));
    }

    @Operation(summary = "reserveSctids")
    @PostMapping("/sct/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSctids(@RequestParam String token, @RequestBody @Valid SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDBulkReserveRequestDto :: {} - AuthenticateResponseDTo :: {}", sctidBulkReservationRequestDto, authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(service.reserveSctids(authToken.getAuthenticateResponseDto(), sctidBulkReservationRequestDto));
    }


}
