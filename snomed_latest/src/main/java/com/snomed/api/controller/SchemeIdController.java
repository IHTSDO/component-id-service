package com.snomed.api.controller;

import com.snomed.api.controller.dto.*;
import com.snomed.api.domain.SchemeId;
import com.snomed.api.domain.SchemeName;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.SchemeIdService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "SchemeIds", value="SchemeIds")
@RestController
public class SchemeIdController {

    @Autowired
    private SchemeIdService schemeIdService;

    @GetMapping("/scheme/ids")
    public ResponseEntity<List<SchemeId>> getSchemeIds(@RequestParam String token,@RequestParam(name="limit",required = false) String limit, @RequestParam(name="skip",required = false) String skip, @RequestParam(name="scheme",required = false) SchemeName scheme) throws APIException {
        return ResponseEntity.ok(schemeIdService.getSchemeIds(token,limit, skip, scheme));
    }

    @GetMapping("/scheme/{schemeName}/ids/{schemeId}")
    public ResponseEntity<SchemeId> getSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @PathVariable String schemeId) throws APIException {
        return ResponseEntity.ok(schemeIdService.getSchemeId(token,schemeName,schemeId));
    }

    @GetMapping("/scheme/{schemeName}/systemids/{systemId}")
    public ResponseEntity<SchemeId> getSchemeIdBySystemId(@RequestParam String token,@PathVariable SchemeName schemeName, @PathVariable String systemId) throws APIException {
        return ResponseEntity.ok(schemeIdService.getSchemeIdsBySystemId(token,schemeName,systemId));
    }

    @PutMapping("/scheme/{schemeName}/deprecate")
    public ResponseEntity<SchemeId> deprecateSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto) throws APIException {
        return ResponseEntity.ok(schemeIdService.deprecateSchemeIds(token,schemeName, schemeIdUpdateRequestDto));
    }
    @PutMapping("/scheme/{schemeName}/release")
    public ResponseEntity<SchemeId> releaseSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto) throws APIException {
        return ResponseEntity.ok(schemeIdService.releaseSchemeIds(token,schemeName, schemeIdUpdateRequestDto));
    }
    @PutMapping("/scheme/{schemeName}/publish")
    public ResponseEntity<SchemeId> publishSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto) throws APIException {
        return ResponseEntity.ok(schemeIdService.publishSchemeId(token,schemeName, schemeIdUpdateRequestDto));
    }

    @PostMapping("/scheme/{schemeName}/reserve")
    public ResponseEntity<SchemeId>   reserveSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName,@RequestBody SchemeIdReserveRequestDto schemeIdReserveRequestDto) throws APIException{
        return ResponseEntity.ok(schemeIdService.reserveSchemeId(token,schemeName,schemeIdReserveRequestDto));
    }

    @PostMapping("/scheme/{schemeName}/generate")
    public ResponseEntity<SchemeId> generateSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdGenerateRequestDto schemeIdGenerateRequestDto) throws APIException
    {

        return ResponseEntity.ok(schemeIdService.generateSchemeId(token,schemeName, schemeIdGenerateRequestDto));
    }
    @PostMapping("/scheme/{schemeName}/register")
    public ResponseEntity<SchemeId>   registerSchemeId(@RequestParam String token,@PathVariable SchemeName schemeName,@RequestBody SchemeIdRegisterRequestDto schemeIdRegisterRequestDto) throws APIException{
        return ResponseEntity.ok(schemeIdService.registerSchemeId(token,schemeName,schemeIdRegisterRequestDto));
    }




}
