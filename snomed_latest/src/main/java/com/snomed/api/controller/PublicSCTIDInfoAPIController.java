package com.snomed.api.controller;

import com.snomed.api.controller.dto.CheckSctidResponseDTO;
import com.snomed.api.controller.dto.NamespacePublicResponse;
import com.snomed.api.domain.Namespace;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.NamespaceService;
import com.snomed.api.service.SctidService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "Public SCTID Info API", value = "Public SCTID Info API")
@RestController
@RequestMapping("/public")
public class PublicSCTIDInfoAPIController {

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    SctidService sctidService;

    @ApiOperation(
            value="Public SCTID Info API",
            notes="Returns public sctID info API"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; publicSCTIDInfoAPIService &ndash; the list of descendants of the concept<br>",tags = { "Public SCTID Info API" })
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })
    @GetMapping("/sct/namespaces")
    @ResponseBody
    public List<Namespace> getNamespaces(@RequestParam(name="token",required = false)String token)
    {
        return namespaceService.getNamespaceslist();
    }

    @GetMapping("/sct/check/{sctid}")
    @ResponseBody
    public CheckSctidResponseDTO checkSctid(@PathVariable String sctid) throws APIException {
        return sctidService.checkSctid(sctid);
    }
}
