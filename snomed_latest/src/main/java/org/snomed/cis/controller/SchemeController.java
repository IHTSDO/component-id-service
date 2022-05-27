package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.snomed.cis.controller.dto.Scheme;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<Scheme>> getSchemesForUser( @PathVariable String username, Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeService.getSchemesForUser(authToken.getAuthenticateResponseDto(),username));
    }

    @GetMapping("/schemes")
    public ResponseEntity<List<SchemeIdBase>> getSchemes(@RequestParam String token) throws CisException {
        return ResponseEntity.ok(schemeService.getSchemes(token));
    }

    @GetMapping("/schemes/{schemeName}")
    public ResponseEntity<SchemeIdBase> getScheme(@PathVariable String schemeName) throws CisException {
        return ResponseEntity.ok(schemeService.getScheme(schemeName));
    }

   // @PutMapping

    @PutMapping("/schemes/{schemeName}")
    public ResponseEntity<String> updateScheme(@PathVariable SchemeName schemeName, @RequestParam String schemeSeq,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(schemeService.updateScheme(authToken.getAuthenticateResponseDto(),schemeName,schemeSeq));
    }
}
