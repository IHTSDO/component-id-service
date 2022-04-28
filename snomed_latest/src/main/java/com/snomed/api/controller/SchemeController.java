package com.snomed.api.controller;

import com.snomed.api.controller.dto.Scheme;
import com.snomed.api.domain.SchemeIdBase;
import com.snomed.api.domain.SchemeName;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.SchemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "Scheme", value = "Scheme")
@RestController
public class SchemeController {

    @Autowired
    public SchemeService schemeService;

    @ApiOperation(
            value="Scheme ",
            notes="Returns a list of scheme "
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; schemeService &ndash; the list of descendants of the concept<br>",tags = { "Scheme " })
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })

    @GetMapping("/users/{username}/schemes/")
    public ResponseEntity<List<Scheme>> getSchemesForUser(@RequestParam String token, @PathVariable String username) throws APIException {
        return ResponseEntity.ok(schemeService.getSchemesForUser(token,username));
    }

    @GetMapping("/schemes")
    public ResponseEntity<List<Scheme>> getSchemes(@RequestParam String token) throws APIException {
        return ResponseEntity.ok(schemeService.getSchemes(token));
    }

    @GetMapping("/schemes/{schemeName}")
    public ResponseEntity<Scheme> getScheme(@RequestParam String token,@PathVariable String schemeName) throws APIException {
        return ResponseEntity.ok(schemeService.getScheme(token,schemeName));
    }

   // @PutMapping

    @PutMapping("/schemes/{schemeName}")
    public ResponseEntity<String> updateScheme(@RequestParam String token,@PathVariable SchemeName schemeName, @RequestParam String schemeSeq) throws APIException {
        return ResponseEntity.ok(schemeService.updateScheme(token,schemeName,schemeSeq));
    }
}
