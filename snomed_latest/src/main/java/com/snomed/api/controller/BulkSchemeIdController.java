package com.snomed.api.controller;

import com.snomed.api.controller.dto.SchemeIdBulkGenerationRequestDto;
import com.snomed.api.controller.dto.SchemeIdBulkRegisterRequestDto;
import com.snomed.api.controller.dto.SchemeIdBulkReserveRequestDto;
import com.snomed.api.domain.BulkJob;
import com.snomed.api.domain.SchemeId;
import com.snomed.api.domain.SchemeName;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.BulkSchemeIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.snomed.api.controller.dto.SchemeIdBulkDeprecateRequestDto;

import java.util.List;

@RestController
public class BulkSchemeIdController {

    @Autowired
    BulkSchemeIdService bulkSchemeIdService;

    @GetMapping("scheme/{schemeName}/bulk")
    public ResponseEntity<List<SchemeId>> getSchemeIds(@PathVariable SchemeName schemeName, @RequestParam String schemeIds) throws APIException {
        return ResponseEntity.ok(bulkSchemeIdService.getSchemeIds(schemeName,schemeIds));
    }

    @PostMapping("scheme/{schemeName}/bulk/generate")
    public ResponseEntity<BulkJob> generateSchemeIds(@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkGenerationRequestDto schemeIdBulkDto) throws APIException {
        return ResponseEntity.ok(bulkSchemeIdService.generateSchemeIds(schemeName,schemeIdBulkDto));
    }

    @PostMapping("scheme/{schemeName}/bulk/register")
    public ResponseEntity<BulkJob> registerSchemeIds(@PathVariable SchemeName schemeName,@RequestBody SchemeIdBulkRegisterRequestDto schemeIdBulkRegisterDto) throws APIException {
        return ResponseEntity.ok(bulkSchemeIdService.registerSchemeIds(schemeName,schemeIdBulkRegisterDto));
    }

        @PostMapping("scheme/{schemeName}/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSchemeIds(@PathVariable SchemeName schemeName,@RequestBody SchemeIdBulkReserveRequestDto schemeIdBulkReserveRequestDto) throws APIException{
        return ResponseEntity.ok(bulkSchemeIdService.reserveSchemeIds(schemeName, schemeIdBulkReserveRequestDto));
    }
    @PutMapping("scheme/{schemeName}/bulk/deprecate")
    public ResponseEntity<BulkJob> deprecateSchemeIds(@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws APIException{
        return ResponseEntity.ok(bulkSchemeIdService.deprecateSchemeIds(schemeName, schemeIdBulkDeprecateRequestDto));
    }

    @PutMapping("scheme/{schemeName}/bulk/release")
    public ResponseEntity<BulkJob> releaseSchemeIds(@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws APIException{
        return ResponseEntity.ok(bulkSchemeIdService.releaseSchemeIds(schemeName, schemeIdBulkDeprecateRequestDto));
    }

    @PutMapping("scheme/{schemeName}/bulk/publish")
    public ResponseEntity<BulkJob> publishSchemeIds(@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws APIException{
        return ResponseEntity.ok(bulkSchemeIdService.publishSchemeIds(schemeName, schemeIdBulkDeprecateRequestDto));
    }
}
