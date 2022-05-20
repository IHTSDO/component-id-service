package org.snomed.cis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.snomed.cis.controller.dto.*;
import org.snomed.cis.domain.Sctid;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.SctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "SCTIDS", value = "SCTIDS")
@RestController
@RequestMapping(path = "/api")
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
    @ResponseBody
    public List<Sctid> getSct(@RequestParam String token, @RequestParam(name = "limit", required = false) String limit, @RequestParam(name = "skip", required = false) String skip, @RequestParam(name = "namespace", required = false) String namespace) throws CisException, JsonProcessingException {
        return sctidService.getSct(token, limit, skip, namespace);
    }

    @GetMapping("/sct/ids/{sctid}")
    @ResponseBody
    public SctWithSchemeResponseDTO getSctWithId(@RequestParam String token, @PathVariable String sctid, @RequestParam(name = "includeAdditionalIds", required = false) String includeAdditionalIds) throws CisException {
        return sctidService.getSctWithId(token, sctid, includeAdditionalIds);
    }

    @GetMapping("/sct/check/{sctid}")
    @ResponseBody
    public CheckSctidResponseDTO checkSctid(@PathVariable String sctid) throws CisException {
        return sctidService.checkSctid(sctid);
    }

    @GetMapping("/sct/namespaces/{namespaceId}/systemids/{systemId}")
    @ResponseBody
    public Sctid getSctBySystemId(@RequestParam String token, @PathVariable Integer namespaceId, @PathVariable String systemId) throws CisException {
        return sctidService.getSctWithSystemId(token, namespaceId, systemId);
    }

    @PutMapping("/sct/deprecate")
    @ResponseBody
    public Sctid deprecateSctid(@RequestParam String token, @RequestBody DeprecateSctRequestDTO deprecateRequest) throws CisException {
        return sctidService.deprecateSct(token, deprecateRequest);
    }

    @PutMapping("/sct/release")
    @ResponseBody
    public Sctid releaseSctid(@RequestParam String token, @RequestBody DeprecateSctRequestDTO deprecateRequest) throws CisException {
        return sctidService.releaseSct(token, deprecateRequest);
    }

    @PutMapping("/sct/publish")
    @ResponseBody
    public Sctid publishSctid(@RequestParam String token, @RequestBody DeprecateSctRequestDTO deprecateRequest) throws CisException {
        return sctidService.publishSct(token, deprecateRequest);
    }

    @PostMapping("/sct/generate")
    @ResponseBody
    public SctWithSchemeResponseDTO generateSctid(@RequestParam String token, @RequestBody SctidsGenerateRequestDto generationData) throws CisException {
        return sctidService.generateSctid(token, generationData);
    }

    @PostMapping("/sct/register")
    @ResponseBody
    public Sctid registerSctid(@RequestParam String token, @RequestBody SCTIDRegistrationRequest generationData) throws CisException {
        return sctidService.registerSctid(token, generationData);
    }

    @PostMapping("/sct/reserve")
    @ResponseBody
    public Sctid reserveSctid(@RequestParam String token, @RequestBody SCTIDReservationRequest reservationData) throws CisException {
        return sctidService.reserveSctid(token, reservationData);
    }
}
