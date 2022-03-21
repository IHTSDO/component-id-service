package com.snomed.api.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snomed.api.controller.dto.BulkJobResponseDto;
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

    @GetMapping("/getSctByIds")
    @ResponseBody
    public List<Sctid> getSctidsByQL(@RequestParam String ids) throws APIException {
        return service.getSctByIds(ids);
    }

    @GetMapping("/test")
    public String getVal()
    {
        return "Test Worked!";

    }

    @GetMapping("/getAllTest")
    public List<Test> getAllTestLst() {
        return service.getAllTest();
    }

    @GetMapping("sct/namespace/{namespaceId}/systemIds")
    public ResponseEntity<List<Sctid>> getSctidBySystemIds(@PathVariable Integer namespaceId, @RequestParam("systemIds") String systemIdStr)
    {
        return ResponseEntity.ok(service.getSctidBySystemIds(systemIdStr,namespaceId));
    }

    //API call for POST /sct/bulk/register
    @PostMapping("/sct/bulk/register")
    public BulkJob registerScts(@RequestBody RegistrationDataDTO registrationData) throws APIException {
        return service.registerSctids(registrationData);
    }

    @PostMapping("/sct/bulk/generate")
    public ResponseEntity<BulkJobResponseDto> generateSctids(@RequestBody @Valid SCTIDBulkGenerationRequestDto sctidBulkGenerationRequestDto) throws JsonProcessingException, APIException {
        return ResponseEntity.ok(service.generateSctids(sctidBulkGenerationRequestDto));
    }

    @PostMapping("/sct/bulk/reserve")
    public ResponseEntity<BulkJob> reserveSctids(@RequestBody @Valid SCTIDBulkReservationRequestDto sctidBulkReservationRequestDto) throws APIException {
        return ResponseEntity.ok(service.reserveSctids(sctidBulkReservationRequestDto));
    }


}
