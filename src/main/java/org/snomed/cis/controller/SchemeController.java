package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.SchemeIdBase;
import org.snomed.cis.domain.SchemeName;
import org.snomed.cis.dto.Scheme;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
@Tag(name = "Scheme" , description = "Scheme Controller")
@RestController
public class SchemeController {
    private final Logger logger = LoggerFactory.getLogger(SchemeController.class);
    @Autowired
    public SchemeService schemeService;

    @Operation(
            summary="Scheme ",
            description="Returns a list of scheme "
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull; schemeService &ndash; the list of descendants of the concept<br>",tags = { "Scheme " })
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })

    @GetMapping("/users/{username}/schemes/")
    public ResponseEntity<List<Scheme>> getSchemesForUser(@RequestParam String token, @PathVariable String username, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - username :: {} - authenticateResponseDto :: {}", username,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(schemeService.getSchemesForUser(authToken.getAuthenticateResponseDto(),username));
    }

    @GetMapping("/schemes")
    public ResponseEntity<List<SchemeIdBase>> getSchemes(@RequestParam String token) throws CisException {
        logger.info("Request received for getSchemes() - No Params");
        return ResponseEntity.ok(schemeService.getSchemes());
    }

    @GetMapping("/schemes/{schemeName}")
    public ResponseEntity<SchemeIdBase> getScheme(@RequestParam String token, @PathVariable String schemeName) throws CisException {
        logger.info("Request received for - schemeName :: {}", schemeName);
        return ResponseEntity.ok(schemeService.getScheme(schemeName));
    }

   // @PutMapping

    @PutMapping("/schemes/{schemeName}")
    public ResponseEntity<String> updateScheme(@PathVariable SchemeName schemeName, @RequestParam String schemeSeq,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - schemeName :: {} - schemeSeq :: {} - authenticateResponseDto :: {}", schemeName,schemeSeq,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(schemeService.updateScheme(authToken.getAuthenticateResponseDto(),schemeName,schemeSeq));
    }
}
