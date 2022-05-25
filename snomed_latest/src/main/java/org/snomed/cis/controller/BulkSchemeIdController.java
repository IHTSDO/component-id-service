package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.snomed.cis.controller.dto.SchemeIdBulkDeprecateRequestDto;
import org.snomed.cis.controller.dto.SchemeIdBulkGenerationRequestDto;
import org.snomed.cis.controller.dto.SchemeIdBulkRegisterRequestDto;
import org.snomed.cis.controller.dto.SchemeIdBulkReserveRequestDto;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.BulkSchemeIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "SchemeIds - Bulk Operations", value = "SchemeIds - Bulk Operations")
@RestController
public class BulkSchemeIdController {

    @Autowired
    BulkSchemeIdService bulkSchemeIdService;

    @ApiOperation(
            value="Bulk Scheme ID Services",
            notes="Returns a list bulk scheme id services"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; bulkSchemeIdService &ndash; the list of descendants of the concept<br>",tags = { "Bulk bulk scheme id services" })
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })
    @GetMapping("scheme/{schemeName}/bulk")
    public ResponseEntity<List<SchemeId>> getSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestParam String schemeIds) throws CisException {
        return ResponseEntity.ok(bulkSchemeIdService.getSchemeIds(token,schemeName,schemeIds));
    }

    @PostMapping("scheme/{schemeName}/bulk/generate")
    public ResponseEntity<BulkJob> generateSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkGenerationRequestDto schemeIdBulkDto) throws CisException {
        return ResponseEntity.ok(bulkSchemeIdService.generateSchemeIds(token,schemeName,schemeIdBulkDto));
    }

    @PostMapping("scheme/{schemeName}/bulk/register")
    public ResponseEntity<BulkJob> registerSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName,@RequestBody SchemeIdBulkRegisterRequestDto schemeIdBulkRegisterDto) throws CisException {
        return ResponseEntity.ok(bulkSchemeIdService.registerSchemeIds(token,schemeName,schemeIdBulkRegisterDto));
    }

        @PostMapping("scheme/{schemeName}/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName,@RequestBody SchemeIdBulkReserveRequestDto schemeIdBulkReserveRequestDto) throws CisException {
        return ResponseEntity.ok(bulkSchemeIdService.reserveSchemeIds(token,schemeName, schemeIdBulkReserveRequestDto));
    }
    @PutMapping("scheme/{schemeName}/bulk/deprecate")
    public ResponseEntity<BulkJob> deprecateSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws CisException {
        return ResponseEntity.ok(bulkSchemeIdService.deprecateSchemeIds(token,schemeName, schemeIdBulkDeprecateRequestDto));
    }

    @PutMapping("scheme/{schemeName}/bulk/release")
    public ResponseEntity<BulkJob> releaseSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws CisException {
        return ResponseEntity.ok(bulkSchemeIdService.releaseSchemeIds(token,schemeName, schemeIdBulkDeprecateRequestDto));
    }

    @PutMapping("scheme/{schemeName}/bulk/publish")
    public ResponseEntity<BulkJob> publishSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws CisException {
        return ResponseEntity.ok(bulkSchemeIdService.publishSchemeIds(token,schemeName, schemeIdBulkDeprecateRequestDto));
    }
}
