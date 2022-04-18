package com.snomed.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snomed.api.controller.dto.*;
import com.snomed.api.domain.Sctid;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.SctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SctidController {
//Test Comment
    @Autowired
    private SctidService sctidService;

    @GetMapping("/test/getsct")
    @ResponseBody
    public Sctid getTestSct(@RequestParam String sctId)
    {
        return sctidService.getTestSct(sctId);
    }

    @GetMapping("/sct/ids")
    @ResponseBody
public List<Sctid> getSct(@RequestParam String token,@RequestParam String limit,@RequestParam String skip,@RequestParam String namespace) throws APIException, JsonProcessingException {
        return sctidService.getSct(token,limit,skip,namespace);
    }

    @GetMapping("/sct/ids/{sctid}")
    @ResponseBody
    public SctWithSchemeResponseDTO getSctWithId(@RequestParam String token, @PathVariable String sctid, @RequestParam String includeAdditionalIds) throws APIException {
        return sctidService.getSctWithId(token,sctid,includeAdditionalIds);
    }

    @GetMapping("/sct/check/{sctid}")
    @ResponseBody
    public CheckSctidResponseDTO checkSctid(@PathVariable String sctid) throws APIException {
        return sctidService.checkSctid(sctid);
    }

    @GetMapping("/sct/namespaces/{namespaceId}/systemids/{systemId}")
    @ResponseBody
    public Sctid getSctBySystemId(@RequestParam String token,@PathVariable Integer namespaceId, @PathVariable String systemId) throws APIException {
        return sctidService.getSctWithSystemId(token,namespaceId,systemId);
    }

    @PutMapping("/sct/deprecate")
    @ResponseBody
    public Sctid deprecateSctid(@RequestParam String token,@RequestBody DeprecateSctRequestDTO deprecateRequest) throws APIException {
        return sctidService.deprecateSct(token,deprecateRequest);
    }
    @PutMapping("/sct/release")
    @ResponseBody
    public Sctid releaseSctid(@RequestParam String token,@RequestBody DeprecateSctRequestDTO deprecateRequest) throws APIException {
        return sctidService.releaseSct(token,deprecateRequest);
    }
    @PutMapping("/sct/publish")
    @ResponseBody
    public Sctid publishSctid(@RequestParam String token,@RequestBody DeprecateSctRequestDTO deprecateRequest) throws APIException {
        return sctidService.publishSct(token,deprecateRequest);
    }
    @PostMapping("/sct/generate")
    @ResponseBody
    public SctWithSchemeResponseDTO generateSctid(@RequestParam String token, @RequestBody SctidsGenerateRequestDto generationData) throws APIException {
        return sctidService.generateSctid(token,generationData);
    }
    @PostMapping("/sct/register")
    @ResponseBody
public Sctid registerSctid(@RequestParam String token, @RequestBody SCTIDRegistrationRequest generationData) throws APIException {
        return sctidService.registerSctid(token,generationData);
    }
    @PostMapping("/sct/reserve")
    @ResponseBody
    public Sctid reserveSctid(@RequestParam String token, @RequestBody SCTIDReservationRequest reservationData) throws APIException {
        return sctidService.reserveSctid(token,reservationData);
    }
}
