package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.Authentication;

import java.util.List;


@Tag(name = "TestService" , description = "Test Controller")
@RestController
public class TestController {

    private Object authentication;

    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })

    @GetMapping("/testService")
    public ResponseEntity<String> testService(){
        return ResponseEntity.ok("Service Ok.");

    }

   /* @GetMapping("/testService")
    public String testService(@RequestParam String token, @ApiIgnore Authentication authentication) throws CisException {
    Token authToken = (Token) authentication;
    ResponseEntity.ok();
    return null;
    } */
}


