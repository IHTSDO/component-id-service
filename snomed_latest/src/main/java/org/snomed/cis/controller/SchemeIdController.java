package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import org.snomed.cis.controller.dto.SchemeIdGenerateRequestDto;
import org.snomed.cis.controller.dto.SchemeIdRegisterRequestDto;
import org.snomed.cis.controller.dto.SchemeIdReserveRequestDto;
import org.snomed.cis.controller.dto.SchemeIdUpdateRequestDto;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SchemeIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "SchemeIds", value="SchemeIds")
@RestController
public class SchemeIdController {

    @Autowired
    private SchemeIdService schemeIdService;

    @GetMapping("/scheme/ids")
    public ResponseEntity<List<SchemeId>> getSchemeIds(@RequestParam String token,@RequestParam(name="limit",required = false) String limit, @RequestParam(name="skip",required = false) String skip, @RequestParam(name="scheme",required = false) SchemeName scheme,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.getSchemeIds(authToken,limit, skip, scheme));
    }

    @GetMapping("/scheme/{schemeName}/ids/{schemeId}")
    public ResponseEntity<SchemeId> getSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @PathVariable String schemeId,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.getSchemeId(authToken,schemeName,schemeId));
    }

    @GetMapping("/scheme/{schemeName}/systemids/{systemId}")
    public ResponseEntity<SchemeId> getSchemeIdBySystemId(@RequestParam String token,@PathVariable SchemeName schemeName, @PathVariable String systemId,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.getSchemeIdsBySystemId(authToken,schemeName,systemId));
    }

    @PutMapping("/scheme/{schemeName}/deprecate")
    public ResponseEntity<SchemeId> deprecateSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.deprecateSchemeIds(authToken,schemeName, schemeIdUpdateRequestDto));
    }
    @PutMapping("/scheme/{schemeName}/release")
    public ResponseEntity<SchemeId> releaseSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.releaseSchemeIds(authToken,schemeName, schemeIdUpdateRequestDto));
    }
    @PutMapping("/scheme/{schemeName}/publish")
    public ResponseEntity<SchemeId> publishSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.publishSchemeId(authToken,schemeName, schemeIdUpdateRequestDto));
    }

    @PostMapping("/scheme/{schemeName}/reserve")
    public ResponseEntity<SchemeId>   reserveSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName,@RequestBody SchemeIdReserveRequestDto schemeIdReserveRequestDto,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.reserveSchemeId(authToken,schemeName,schemeIdReserveRequestDto));
    }

    @PostMapping("/scheme/{schemeName}/generate")
    public ResponseEntity<SchemeId> generateSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdGenerateRequestDto schemeIdGenerateRequestDto,Authentication authentication) throws CisException
    {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.generateSchemeId(authToken,schemeName, schemeIdGenerateRequestDto));
    }
    @PostMapping("/scheme/{schemeName}/register")
    public ResponseEntity<SchemeId>   registerSchemeId(@RequestParam String token, @PathVariable SchemeName schemeName, @RequestBody SchemeIdRegisterRequestDto schemeIdRegisterRequestDto, Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeIdService.registerSchemeId(authToken,schemeName,schemeIdRegisterRequestDto));
    }
}
