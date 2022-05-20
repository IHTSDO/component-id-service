package org.snomed.cis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.snomed.cis.controller.dto.*;
import org.snomed.cis.domain.BulkJob;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.repository.SctidRepository;
import org.snomed.cis.service.BulkSctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "SCTIDS - Bulk Operations", value = "SCTIDS - Bulk Operations")
@RestController
@RequestMapping(path = "/api")
public class BulkSctidController {
    @Autowired
    private BulkSctidService service;

    @Autowired
    SctidRepository sctidRepository;

    @Autowired
    HttpServletResponse httpResponse;

    @Autowired
    HttpServletRequest httpRequest;

    @ApiOperation(
            value = "Bulk Sct ID",
            notes = "Returns a list Sct ID"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; BulkSctidService &ndash; the list of descendants of the concept<br>", tags = {"Bulk Sct ID"})
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })

    @GetMapping("/sct/bulk/ids")
    @ResponseBody
    public List<Sctid> getSctidsByQL(@RequestParam String token, @RequestParam String ids) throws CisException {
        return service.getSctByIds(token, ids);
    }

    @PostMapping("/sct/bulk/ids")
    @ResponseBody
    public List<Sctid> getSctidsByQLPost(@RequestParam String token, @RequestBody SctIdRequest sctids) throws CisException {
        return service.postSctByIds(token, sctids);
    }

    @GetMapping("/test")
    public String getVal() {
        return "Test Worked!";

    }

    @GetMapping("sct/namespace/{namespaceId}/systemIds")
    public ResponseEntity<List<Sctid>> getSctidBySystemIds(@RequestParam String token, @PathVariable Integer namespaceId, @RequestParam("systemIds") String systemIdStr) {
        return ResponseEntity.ok(service.getSctidBySystemIds(token, systemIdStr, namespaceId));
    }

    //API call for POST /sct/bulk/register
    @PostMapping("/sct/bulk/register")
    public BulkJob registerScts(@RequestParam String token, @RequestBody RegistrationDataDTO registrationData) throws CisException {
        return service.registerSctids(token, registrationData);
    }

    @PostMapping("/sct/bulk/generate")
    public ResponseEntity<BulkJobResponseDto> generateSctids(@RequestParam String token, @RequestBody @Valid SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto) throws JsonProcessingException, CisException {
        return ResponseEntity.ok(service.generateSctids(token, sctidBulkGenerationRequestDto));
    }

    @PutMapping("/sct/bulk/deprecate")
    public BulkJob deprecateSctid(@RequestParam String token, @RequestBody
            BulkSctRequestDTO deprecationData) throws CisException {
        return service.deprecateSctid(token, deprecationData);
    }

    @PutMapping("/sct/bulk/publish")
    public BulkJob publishSctid(@RequestParam String token, @RequestBody
            BulkSctRequestDTO publishData) throws CisException {
        return service.publishSctid(token, publishData);
    }

    @PutMapping("/sct/bulk/release")
    public BulkJob releaseSctid(@RequestParam String token, @RequestBody
            BulkSctRequestDTO publishData) throws CisException {
        return service.releaseSctid(token, publishData);
    }

    @PostMapping("/sct/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSctids(@RequestParam String token, @RequestBody @Valid SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto) throws CisException {
        return ResponseEntity.ok(service.reserveSctids(token, sctidBulkReservationRequestDto));
    }


}
