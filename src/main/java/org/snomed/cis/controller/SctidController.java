package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@Tag(name = "SCTIDS" , description = "Sctid Controller")
@RestController
public class SctidController {
    private final Logger logger = LoggerFactory.getLogger(SctidController.class);

    @Autowired
    private SctidService sctidService;

    @Operation(
            summary = "SCTIDS",
            description = "Returns a list of sct IDs"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; sctidService &ndash; the list of descendants of the concept<br>", tags = {"SCTIDS"})
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })

    @GetMapping("/sct/ids")
    public ResponseEntity<List<Sctid>> getSct(@RequestParam String token, @RequestParam(name = "limit", required = false) String limit, @RequestParam(name = "skip", required = false) String skip, @RequestParam(name = "namespace", required = false) String namespace, @Parameter(hidden = true) Authentication authentication) throws CisException{
        Token authToken = (Token) authentication;
        logger.info("Request received for - limit :: {} - skip :: {} - namespace :: {} - authenticateResponseDto :: {}", limit,skip,namespace,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.getSct(authToken.getAuthenticateResponseDto(), limit, skip, namespace));
    }

    @GetMapping("/sct/ids/{sctid}")
    public ResponseEntity<SctWithSchemeResponseDTO> getSctWithId(@RequestParam String token, @PathVariable String sctid, @RequestParam(name = "includeAdditionalIds", required = false) String includeAdditionalIds,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - sctid :: {} - includeAdditionalIds :: {} - authenticateResponseDto :: {}", sctid,includeAdditionalIds,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.getSctWithId(authToken.getAuthenticateResponseDto(), sctid, includeAdditionalIds));
    }

    @GetMapping("/sct/check/{sctid}")
    public ResponseEntity<CheckSctidResponseDTO> checkSctid(@RequestParam(required = false) String token, @PathVariable String sctid) throws CisException {
        logger.info("Request received for - sctid :: {} ", sctid);
        return ResponseEntity.ok(sctidService.checkSctid(sctid));
    }

    @GetMapping("/sct/namespaces/{namespaceId}/systemids/{systemId}")
    public ResponseEntity<Sctid> getSctBySystemId(@RequestParam String token, @PathVariable Integer namespaceId, @PathVariable String systemId,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - namespaceId :: {} - systemId :: {} - authenticateResponseDto :: {}", namespaceId,systemId,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.getSctWithSystemId(authToken.getAuthenticateResponseDto(), namespaceId, systemId));
    }

    @PutMapping("/sct/deprecate")
    public ResponseEntity<Sctid> deprecateSctid(@RequestParam String token, @RequestBody DeprecateSctRequestDTO deprecateRequest,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - DeprecateSctRequestDTO :: {} - authenticateResponseDto :: {}", deprecateRequest,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.deprecateSct(authToken.getAuthenticateResponseDto(), deprecateRequest));
    }

    @PutMapping("/sct/release")
    public ResponseEntity<Sctid> releaseSctid(@RequestParam String token, @RequestBody DeprecateSctRequestDTO deprecateRequest,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - DeprecateSctRequestDTO :: {} - authenticateResponseDto :: {}", deprecateRequest,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.releaseSct(authToken.getAuthenticateResponseDto(), deprecateRequest));
    }

    @PutMapping("/sct/publish")
    public ResponseEntity<Sctid> publishSctid(@RequestParam String token, @RequestBody DeprecateSctRequestDTO deprecateRequest,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - DeprecateSctRequestDTO :: {} - authenticateResponseDto :: {}", deprecateRequest,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.publishSct(authToken.getAuthenticateResponseDto(), deprecateRequest));
    }

    @PostMapping("/sct/generate")
    public ResponseEntity<SctWithSchemeResponseDTO> generateSctid(@RequestParam String token, @RequestBody SctidsGenerateRequestDto generationData, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SctidsGenerateRequestDto :: {} - authenticateResponseDto :: {}", generationData,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.generateSctid(authToken.getAuthenticateResponseDto(), generationData));
    }

    @PostMapping("/sct/register")
    public ResponseEntity<Sctid> registerSctid(@RequestParam String token, @RequestBody SCTIDRegistrationRequest generationData,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDRegistrationRequest :: {} - authenticateResponseDto :: {}", generationData,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.registerSctid(authToken.getAuthenticateResponseDto(), generationData));
    }

    @PostMapping("/sct/reserve")
    public ResponseEntity<Sctid> reserveSctid(@RequestParam String token, @RequestBody SCTIDReservationRequest reservationData,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - SCTIDReservationRequest :: {} - authenticateResponseDto :: {}", reservationData,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(sctidService.reserveSctid(authToken.getAuthenticateResponseDto(), reservationData));
    }
}
