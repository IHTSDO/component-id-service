package org.snomed.cis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.snomed.cis.controller.dto.*;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "SCTIDS", value = "SCTIDS")
@RestController
public class SctidController {
    //Test Comment
    @Autowired
    private SctidService sctidService;

    @ApiOperation(
            value = "SCTIDS",
            notes = "Returns a list of sct IDs"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; sctidService &ndash; the list of descendants of the concept<br>", tags = {"SCTIDS"})
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })

    @GetMapping("/sct/ids")
    public ResponseEntity<List<Sctid>> getSct( @RequestParam(name = "limit", required = false) String limit, @RequestParam(name = "skip", required = false) String skip, @RequestParam(name = "namespace", required = false) String namespace,Authentication authentication) throws CisException, JsonProcessingException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.getSct(authToken.getAuthenticateResponseDto(), limit, skip, namespace));
    }

    @GetMapping("/sct/ids/{sctid}")
    public ResponseEntity<SctWithSchemeResponseDTO> getSctWithId( @PathVariable String sctid, @RequestParam(name = "includeAdditionalIds", required = false) String includeAdditionalIds,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.getSctWithId(authToken.getAuthenticateResponseDto(), sctid, includeAdditionalIds));
    }

    @GetMapping("/sct/check/{sctid}")
    public ResponseEntity<CheckSctidResponseDTO> checkSctid(@PathVariable String sctid) throws CisException {
        return ResponseEntity.ok(sctidService.checkSctid(sctid));
    }

    @GetMapping("/sct/namespaces/{namespaceId}/systemids/{systemId}")
    public ResponseEntity<Sctid> getSctBySystemId( @PathVariable Integer namespaceId, @PathVariable String systemId,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.getSctWithSystemId(authToken.getAuthenticateResponseDto(), namespaceId, systemId));
    }

    @PutMapping("/sct/deprecate")
    public ResponseEntity<Sctid> deprecateSctid( @RequestBody DeprecateSctRequestDTO deprecateRequest,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.deprecateSct(authToken.getAuthenticateResponseDto(), deprecateRequest));
    }

    @PutMapping("/sct/release")
    public ResponseEntity<Sctid> releaseSctid( @RequestBody DeprecateSctRequestDTO deprecateRequest,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.releaseSct(authToken.getAuthenticateResponseDto(), deprecateRequest));
    }

    @PutMapping("/sct/publish")
    public ResponseEntity<Sctid> publishSctid( @RequestBody DeprecateSctRequestDTO deprecateRequest,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.publishSct(authToken.getAuthenticateResponseDto(), deprecateRequest));
    }

    @PostMapping("/sct/generate")
    public ResponseEntity<SctWithSchemeResponseDTO> generateSctid( @RequestBody SctidsGenerateRequestDto generationData, Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.generateSctid(authToken.getAuthenticateResponseDto(), generationData));
    }

    @PostMapping("/sct/register")
    public ResponseEntity<Sctid> registerSctid( @RequestBody SCTIDRegistrationRequest generationData,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.registerSctid(authToken.getAuthenticateResponseDto(), generationData));
    }

    @PostMapping("/sct/reserve")
    public ResponseEntity<Sctid> reserveSctid( @RequestBody SCTIDReservationRequest reservationData,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(sctidService.reserveSctid(authToken.getAuthenticateResponseDto(), reservationData));
    }
}
