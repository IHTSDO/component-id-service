package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.dto.CheckSctidResponseDTO;
import org.snomed.cis.dto.NamespaceDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.NamespaceService;
import org.snomed.cis.service.SctidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "Public SCTID Info API", value = "Public SCTID Info API")
@RestController
@RequestMapping("/public")
public class PublicSCTIDInfoAPIController {
    private final Logger logger = LoggerFactory.getLogger(PublicSCTIDInfoAPIController.class);
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
    public List<NamespaceDto> getNamespaces(@RequestParam String token)
    {
        logger.info("Request received for getNamespaces() - No Param");
        return namespaceService.getNamespaceslist();
    }

    @GetMapping("/sct/check/{sctid}")
    @ResponseBody
    public CheckSctidResponseDTO checkSctid(@RequestParam String token, @PathVariable String sctid) throws CisException {
        logger.info("Request received for - sctid :: {}", sctid);
        return sctidService.checkSctid(sctid);
    }
}
