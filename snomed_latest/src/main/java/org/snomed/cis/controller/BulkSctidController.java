package org.snomed.cis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.controller.dto.*;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.BulkSctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "SCTIDS - Bulk Operations", value = "SCTIDS - Bulk Operations")
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

    @ApiOperation(
            value = "Bulk Sct ID",
            notes = "Returns a list Sct ID"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; BulkSctidService &ndash; the list of descendants of the concept<br>", tags = {"Bulk Sct ID"})
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })

    @GetMapping("/sct/bulk/ids")
    public ResponseEntity<List<Sctid>> getSctidsByQL( @RequestParam String ids) throws CisException {
        logger.info("Request received for - ids :: {}", ids);
        return ResponseEntity.ok(service.getSctByIds(ids));
    }

    @PostMapping("/sct/bulk/ids")
    public ResponseEntity<List<Sctid>> getSctidsByQLPost( @RequestBody SctIdRequest sctids) throws CisException {
        logger.info("Request received for - sctids :: {}", sctids);
        return ResponseEntity.ok(service.postSctByIds(sctids));
    }

    @GetMapping("sct/namespace/{namespaceId}/systemIds")
    public ResponseEntity<List<Sctid>> getSctidBySystemIds( @PathVariable Integer namespaceId, @RequestParam("systemIds") String systemIdStr) {
        logger.info("Request received for - namespaceId :: {} - systemIdStr :: {}", namespaceId,systemIdStr);
        return ResponseEntity.ok(service.getSctidBySystemIds(systemIdStr, namespaceId));
    }

    @PostMapping("/sct/bulk/register")
    public ResponseEntity<BulkJob> registerScts( @RequestBody RegistrationDataDTO registrationData,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - RegistrationDataDTO :: {} - AuthenticateResponseDTo :: {}", registrationData,authToken.getAuthenticateResponseDto());
        return ResponseEntity.ok(service.registerSctids(authToken.getAuthenticateResponseDto(), registrationData));
    }

    @PostMapping("/sct/bulk/generate")
    public ResponseEntity<BulkJobResponseDto> generateSctids( @RequestBody @Valid SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto, Authentication authentication) throws JsonProcessingException, CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDBulkGenerationRequestDto :: {} - AuthenticateResponseDTo :: {}", sctidBulkGenerationRequestDto,authToken.getAuthenticateResponseDto());
        return ResponseEntity.ok(service.generateSctids(authToken.getAuthenticateResponseDto(), sctidBulkGenerationRequestDto));
    }

    @PutMapping("/sct/bulk/deprecate")
    public ResponseEntity<BulkJob> deprecateSctid( @RequestBody
            BulkSctRequestDTO deprecationData, Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - BulkSctRequestDTO :: {} - AuthenticateResponseDTo :: {}", deprecationData,authToken.getAuthenticateResponseDto());
        return ResponseEntity.ok(service.deprecateSctid(authToken.getAuthenticateResponseDto(), deprecationData));
    }

    @PutMapping("/sct/bulk/publish")
    public ResponseEntity<BulkJob> publishSctid( @RequestBody
            BulkSctRequestDTO publishData, Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDBulkPublishRequestDto :: {} - AuthenticateResponseDTo :: {}", publishData,authToken.getAuthenticateResponseDto());
        return ResponseEntity.ok(service.publishSctid(authToken.getAuthenticateResponseDto(), publishData));
    }

    @PutMapping("/sct/bulk/release")
    public ResponseEntity<BulkJob> releaseSctid( @RequestBody
            BulkSctRequestDTO publishData, Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDBulkReleaseRequestDto :: {} - AuthenticateResponseDTo :: {}", publishData,authToken.getAuthenticateResponseDto());
        return ResponseEntity.ok(service.releaseSctid(authToken.getAuthenticateResponseDto(), publishData));
    }

    @PostMapping("/sct/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSctids( @RequestBody @Valid SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto, Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDBulkReserveRequestDto :: {} - AuthenticateResponseDTo :: {}", sctidBulkReservationRequestDto,authToken.getAuthenticateResponseDto());
        return ResponseEntity.ok(service.reserveSctids(authToken.getAuthenticateResponseDto(), sctidBulkReservationRequestDto));
    }


}
