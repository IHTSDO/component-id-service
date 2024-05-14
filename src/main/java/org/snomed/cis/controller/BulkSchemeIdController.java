package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.SchemeIdBulkDeprecateRequestDto;
import org.snomed.cis.dto.SchemeIdBulkGenerationRequestDto;
import org.snomed.cis.dto.SchemeIdBulkRegisterRequestDto;
import org.snomed.cis.dto.SchemeIdBulkReserveRequestDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.BulkSchemeIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
@Tag(name = "SchemeIds - Bulk Operations" , description = "Bulk Scheme Id Controller")
@RestController
public class BulkSchemeIdController {
    private final Logger logger = LoggerFactory.getLogger(BulkSchemeIdController.class);
    @Autowired
    BulkSchemeIdService bulkSchemeIdService;

    @Operation(
            summary="Bulk Scheme ID Services",
            description="Returns a list bulk scheme id services"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; bulkSchemeIdService &ndash; the list of descendants of the concept<br>",tags = { "Bulk bulk scheme id services" })
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })
    @GetMapping("scheme/{schemeName}/bulk")
    public ResponseEntity<List<SchemeId>> getSchemeIds(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestParam String schemeIds,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIds :: {} - authenticateResponseDto :: {}", schemeName,schemeIds,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(bulkSchemeIdService.getSchemeIds(authToken.getAuthenticateResponseDto(),schemeName,schemeIds));
    }

    @Operation(summary = "generateSchemeIds")
    @PostMapping("scheme/{schemeName}/bulk/generate")
    public ResponseEntity<BulkJob> generateSchemeIds(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkGenerationRequestDto schemeIdBulkDto,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdBulkDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdBulkDto,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(bulkSchemeIdService.generateSchemeIds(authToken.getAuthenticateResponseDto(),schemeName,schemeIdBulkDto));
    }

    @Operation(summary = "registerSchemeIds")
    @PostMapping("scheme/{schemeName}/bulk/register")
    public ResponseEntity<BulkJob> registerSchemeIds(@RequestParam String token, @PathVariable SchemeName schemeName,@RequestBody SchemeIdBulkRegisterRequestDto schemeIdBulkRegisterDto,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdBulkRegisterDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdBulkRegisterDto,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(bulkSchemeIdService.registerSchemeIds(authToken.getAuthenticateResponseDto(),schemeName,schemeIdBulkRegisterDto));
    }

    @Operation(summary = "reserveSchemeIds")
    @PostMapping("scheme/{schemeName}/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSchemeIds(@RequestParam String token, @PathVariable SchemeName schemeName,@RequestBody SchemeIdBulkReserveRequestDto schemeIdBulkReserveRequestDto,@Parameter(hidden = true) Authentication authentication) throws CisException {
            Token authToken = (Token) authentication;
            logger.info("Request received for - schemeName :: {} - schemeIdBulkRegisterDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdBulkReserveRequestDto,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(bulkSchemeIdService.reserveSchemeIds(authToken.getAuthenticateResponseDto(),schemeName, schemeIdBulkReserveRequestDto));
    }
    @Operation(summary = "deprecateSchemeIds")
    @PutMapping("scheme/{schemeName}/bulk/deprecate")
    public ResponseEntity<BulkJob> deprecateSchemeIds(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdBulkDeprecateRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdBulkDeprecateRequestDto,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(bulkSchemeIdService.deprecateSchemeIds(authToken.getAuthenticateResponseDto(),schemeName, schemeIdBulkDeprecateRequestDto));
    }

    @Operation(summary = "releaseSchemeIds")
    @PutMapping("scheme/{schemeName}/bulk/release")
    public ResponseEntity<BulkJob> releaseSchemeIds(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdBulkReleaseRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdBulkDeprecateRequestDto,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(bulkSchemeIdService.releaseSchemeIds(authToken.getAuthenticateResponseDto(),schemeName, schemeIdBulkDeprecateRequestDto));
    }

    @Operation(summary = "publishSchemeIds")
    @PutMapping("scheme/{schemeName}/bulk/publish")
    public ResponseEntity<BulkJob> publishSchemeIds(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdBulkPublishRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdBulkDeprecateRequestDto,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(bulkSchemeIdService.publishSchemeIds(authToken.getAuthenticateResponseDto(),schemeName, schemeIdBulkDeprecateRequestDto));
    }
}
