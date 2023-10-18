package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.SchemeIdGenerateRequestDto;
import org.snomed.cis.dto.SchemeIdRegisterRequestDto;
import org.snomed.cis.dto.SchemeIdReserveRequestDto;
import org.snomed.cis.dto.SchemeIdUpdateRequestDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SchemeIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(tags = "SchemeIds", value="SchemeIds")
@RestController
public class SchemeIdController {
    private final Logger logger = LoggerFactory.getLogger(SchemeIdController.class);
    @Autowired
    private SchemeIdService schemeIdService;

    @GetMapping("/scheme/ids")
    public ResponseEntity<List<SchemeId>> getSchemeIds(@RequestParam String token, @RequestParam(name="limit",required = false) String limit, @RequestParam(name="skip",required = false) String skip, @RequestParam(name="scheme",required = false) SchemeName scheme,@ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - limit :: {} - skip :: {} - scheme :: {} - authenticateResponseDto :: {}", limit,skip,scheme,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.getSchemeIds(authToken.getAuthenticateResponseDto(),limit, skip, scheme));
    }

    @GetMapping("/scheme/{schemeName}/ids/{schemeId}")
    public ResponseEntity<SchemeId> getSchemeId(@RequestParam String token, @PathVariable SchemeName schemeName, @PathVariable String schemeId,@ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeId :: {} - authenticateResponseDto :: {}", schemeName,schemeId,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.getSchemeId(authToken.getAuthenticateResponseDto(),schemeName,schemeId));
    }

    @GetMapping("/scheme/{schemeName}/systemids/{systemId}")
    public ResponseEntity<SchemeId> getSchemeIdBySystemId(@RequestParam String token, @PathVariable SchemeName schemeName, @PathVariable String systemId,@ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - systemId :: {} - authenticateResponseDto :: {}", schemeName,systemId,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.getSchemeIdsBySystemId(authToken.getAuthenticateResponseDto(),schemeName,systemId));
    }

    @PutMapping("/scheme/{schemeName}/deprecate")
    public ResponseEntity<SchemeId> deprecateSchemeId(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto,@ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdUpdateRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdUpdateRequestDto,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.deprecateSchemeIds(authToken.getAuthenticateResponseDto(),schemeName, schemeIdUpdateRequestDto));
    }
    @PutMapping("/scheme/{schemeName}/release")
    public ResponseEntity<SchemeId> releaseSchemeId(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto,@ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdUpdateRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdUpdateRequestDto,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.releaseSchemeIds(authToken.getAuthenticateResponseDto(),schemeName, schemeIdUpdateRequestDto));
    }
    @PutMapping("/scheme/{schemeName}/publish")
    public ResponseEntity<SchemeId> publishSchemeId(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto,@ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdUpdateRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdUpdateRequestDto,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.publishSchemeId(authToken.getAuthenticateResponseDto(),schemeName, schemeIdUpdateRequestDto));
    }

    @PostMapping("/scheme/{schemeName}/reserve")
    public ResponseEntity<SchemeId>   reserveSchemeId(@RequestParam String token, @PathVariable SchemeName schemeName,@RequestBody SchemeIdReserveRequestDto schemeIdReserveRequestDto,@ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeIdReserveRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdReserveRequestDto,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.reserveSchemeId(authToken.getAuthenticateResponseDto(),schemeName,schemeIdReserveRequestDto));
    }

    @PostMapping("/scheme/{schemeName}/generate")
    public ResponseEntity<SchemeId> generateSchemeId(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdGenerateRequestDto schemeIdGenerateRequestDto,@ApiIgnore Authentication authentication) throws CisException
    {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - SchemeIdGenerateRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdGenerateRequestDto,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.generateSchemeId(authToken.getAuthenticateResponseDto(),schemeName, schemeIdGenerateRequestDto));
    }
    @PostMapping("/scheme/{schemeName}/register")
    public ResponseEntity<SchemeId>   registerSchemeId(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdRegisterRequestDto schemeIdRegisterRequestDto, @ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - SchemeIdRegisterRequestDto :: {} - authenticateResponseDto :: {}", schemeName,schemeIdRegisterRequestDto,authToken.getAuthenticateResponseDto().getDisplayName());
        return ResponseEntity.ok(schemeIdService.registerSchemeId(authToken.getAuthenticateResponseDto(),schemeName,schemeIdRegisterRequestDto));
    }
}
