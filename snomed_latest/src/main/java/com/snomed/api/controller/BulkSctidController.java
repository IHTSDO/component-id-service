package com.snomed.api.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snomed.api.controller.dto.BulkJobResponseDto;
import com.snomed.api.controller.dto.BulkSctRequestDTO;
import com.snomed.api.controller.dto.RegistrationDataDTO;
import com.snomed.api.controller.dto.SCTIDBulkGenerationRequestDto;
import com.snomed.api.controller.dto.SCTIDBulkReservationRequestDto;
import com.snomed.api.domain.BulkJob;
import com.snomed.api.domain.Test;
import com.snomed.api.exception.APIException;
import com.snomed.api.repository.SctidRepository;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.snomed.api.domain.Sctid;
import com.snomed.api.service.BulkSctidService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class BulkSctidController {
    @Autowired
    private BulkSctidService service;

    @Autowired
    SctidRepository sctidRepository;

    @Autowired
    HttpServletResponse httpResponse;

    @Autowired
    HttpServletRequest httpRequest;


    @GetMapping("/sct/bulk/ids")
    @ResponseBody
    public List<Sctid> getSctidsByQL(@RequestParam String token,@RequestParam String ids) throws APIException {
        return service.getSctByIds(token,ids);
    }

    @PostMapping("/sct/bulk/ids")
    @ResponseBody
    public List<Sctid> getSctidsByQLPost(@RequestParam String token,@RequestBody String sctids) throws APIException {
        return service.getSctByIds(token,sctids);
    }

    @GetMapping("/test")
    public String getVal()
    {
        return "Test Worked!";

    }

    @GetMapping("/getAllTest")
    public List<Sctid> getAllTestLst() {
        return service.getAllTest();
    }

    @GetMapping("sct/namespace/{namespaceId}/systemIds")
    public ResponseEntity<List<Sctid>> getSctidBySystemIds(@RequestParam String token,@PathVariable Integer namespaceId, @RequestParam("systemIds") String systemIdStr)
    {
        return ResponseEntity.ok(service.getSctidBySystemIds(token,systemIdStr,namespaceId));
    }

    //API call for POST /sct/bulk/register
    @PostMapping("/sct/bulk/register")
    public BulkJob registerScts(@RequestParam String token,@RequestBody RegistrationDataDTO registrationData) throws APIException {
        return service.registerSctids(token,registrationData);
    }

    @PostMapping("/sct/bulk/generate")
    public ResponseEntity<BulkJobResponseDto> generateSctids(@RequestParam String token,@RequestBody @Valid SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto) throws JsonProcessingException, APIException {
        return ResponseEntity.ok(service.generateSctids(token,sctidBulkGenerationRequestDto));
    }

    @PutMapping("/sct/bulk/deprecate")
    public BulkJob deprecateSctid (@RequestParam String token,@RequestBody
    BulkSctRequestDTO deprecationData) throws APIException {
        return service.deprecateSctid(token,deprecationData);
    }

    @PutMapping("/sct/bulk/publish")
    public BulkJob publishSctid (@RequestParam String token,@RequestBody
            BulkSctRequestDTO publishData) throws APIException {
        return service.publishSctid(token,publishData);
    }
    @PutMapping("/sct/bulk/release")
    public BulkJob releaseSctid (@RequestParam String token,@RequestBody
            BulkSctRequestDTO publishData) throws APIException {
        return service.releaseSctid(token,publishData);
    }

    @PostMapping("/sct/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSctids(@RequestBody @Valid SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto) throws APIException {
        return ResponseEntity.ok(service.reserveSctids(sctidBulkReservationRequestDto));
    }


}
