package com.snomed.api.controller;

import com.snomed.api.controller.dto.CheckSctidResponseDTO;
import com.snomed.api.controller.dto.NamespacePublicResponse;
import com.snomed.api.domain.Namespace;
import com.snomed.api.exception.APIException;
import com.snomed.api.helper.SctIdHelper;
import com.snomed.api.service.PublicSCTIDInfoAPIService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "Public SCTID Info API", value = "Public SCTID Info API")
@RestController
@RequestMapping("/public")
public class PublicSCTIDInfoAPIController {
    @Autowired
    PublicSCTIDInfoAPIService publicSCTIDInfoAPIService;

    @GetMapping("/sct/namespaces")
    @ResponseBody
    public List<Namespace> getNamespaces()
    {
        return publicSCTIDInfoAPIService.getNamespaces();
    }

    @GetMapping("/sct/check/{sctid}")
    @ResponseBody
    public CheckSctidResponseDTO checkSctid(@PathVariable String sctid) throws APIException {
        return publicSCTIDInfoAPIService.checkSctid(sctid);
    }
}
