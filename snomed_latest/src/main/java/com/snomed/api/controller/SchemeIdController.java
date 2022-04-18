package com.snomed.api.controller;

import com.snomed.api.controller.dto.SchemeIdUpdateRequestDto;
import com.snomed.api.domain.SchemeId;
import com.snomed.api.domain.SchemeName;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.SchemeIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SchemeIdController {

    @Autowired
    private SchemeIdService schemeIdService;

    @GetMapping("/scheme/ids")
    public ResponseEntity<List<SchemeId>> getSchemeIds(@RequestParam String limit, @RequestParam String skip, @PathVariable SchemeName scheme) throws APIException {
        return ResponseEntity.ok(schemeIdService.getSchemeIds(limit, skip, scheme));
    }

    @GetMapping("/scheme/{schemeName}/ids/{schemeId}")
    public ResponseEntity<SchemeId> getSchemeIdsBySchemeId(@PathVariable SchemeName schemeName, @PathVariable String schemeid) throws APIException {
        return ResponseEntity.ok(schemeIdService.getSchemeIdsByschemeId(schemeName,schemeid));
    }

    @GetMapping("/scheme/{schemeName}/systemids/{systemId}")
    public ResponseEntity<SchemeId> getSchemeIdBySystemId(@PathVariable SchemeName schemeName, @PathVariable String systemid) throws APIException {
        return ResponseEntity.ok(schemeIdService.getSchemeIdsBySystemId(schemeName,systemid));
    }

    @PutMapping("/scheme/{schemeName}/deprecate")
    public ResponseEntity<SchemeId> deprecateSchemeId(@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto) throws APIException {
        return ResponseEntity.ok(schemeIdService.deprecateSchemeIds(schemeName, schemeIdUpdateRequestDto));
    }
    @PutMapping("/scheme/{schemeName}/release")
    public ResponseEntity<SchemeId> releaseSchemeId(@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto) throws APIException {
        return ResponseEntity.ok(schemeIdService.releaseSchemeIds(schemeName, schemeIdUpdateRequestDto));
    }
    @PutMapping("/scheme/{schemeName}/publish")
    public ResponseEntity<SchemeId> publishSchemeId(@PathVariable SchemeName schemeName, @RequestBody SchemeIdUpdateRequestDto schemeIdUpdateRequestDto) throws APIException {
        return ResponseEntity.ok(schemeIdService.publishSchemeId(schemeName, schemeIdUpdateRequestDto));
    }
}
