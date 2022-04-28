package com.snomed.api.controller;

import com.snomed.api.controller.dto.SchemeIdBulkDeprecateRequestDto;
import com.snomed.api.controller.dto.SchemeIdBulkGenerationRequestDto;
import com.snomed.api.controller.dto.SchemeIdBulkRegisterRequestDto;
import com.snomed.api.controller.dto.SchemeIdBulkReserveRequestDto;
import com.snomed.api.domain.BulkJob;
import com.snomed.api.domain.SchemeId;
import com.snomed.api.domain.SchemeName;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.BulkSchemeIdService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.snomed.api.controller.dto.SchemeIdBulkDeprecateRequestDto;

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
    public ResponseEntity<List<SchemeId>> getSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestParam String schemeIds) throws APIException {
        return ResponseEntity.ok(bulkSchemeIdService.getSchemeIds(token,schemeName,schemeIds));
    }

    @PostMapping("scheme/{schemeName}/bulk/generate")
    public ResponseEntity<BulkJob> generateSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkGenerationRequestDto schemeIdBulkDto) throws APIException {
        return ResponseEntity.ok(bulkSchemeIdService.generateSchemeIds(token,schemeName,schemeIdBulkDto));
    }

    @PostMapping("scheme/{schemeName}/bulk/register")
    public ResponseEntity<BulkJob> registerSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName,@RequestBody SchemeIdBulkRegisterRequestDto schemeIdBulkRegisterDto) throws APIException {
        return ResponseEntity.ok(bulkSchemeIdService.registerSchemeIds(token,schemeName,schemeIdBulkRegisterDto));
    }

        @PostMapping("scheme/{schemeName}/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName,@RequestBody SchemeIdBulkReserveRequestDto schemeIdBulkReserveRequestDto) throws APIException{
        return ResponseEntity.ok(bulkSchemeIdService.reserveSchemeIds(token,schemeName, schemeIdBulkReserveRequestDto));
    }
    @PutMapping("scheme/{schemeName}/bulk/deprecate")
    public ResponseEntity<BulkJob> deprecateSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws APIException{
        return ResponseEntity.ok(bulkSchemeIdService.deprecateSchemeIds(token,schemeName, schemeIdBulkDeprecateRequestDto));
    }

    @PutMapping("scheme/{schemeName}/bulk/release")
    public ResponseEntity<BulkJob> releaseSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws APIException{
        return ResponseEntity.ok(bulkSchemeIdService.releaseSchemeIds(token,schemeName, schemeIdBulkDeprecateRequestDto));
    }

    @PutMapping("scheme/{schemeName}/bulk/publish")
    public ResponseEntity<BulkJob> publishSchemeIds(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestBody SchemeIdBulkDeprecateRequestDto schemeIdBulkDeprecateRequestDto) throws APIException{
        return ResponseEntity.ok(bulkSchemeIdService.publishSchemeIds(token,schemeName, schemeIdBulkDeprecateRequestDto));
    }
}
