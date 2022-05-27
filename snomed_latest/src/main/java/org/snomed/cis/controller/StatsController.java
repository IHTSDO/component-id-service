package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import org.snomed.cis.controller.dto.GetStatsResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Stats", value = "Stats")
@RestController
public class StatsController {

    @Autowired
    StatsService statsService;


    @GetMapping("/stats")
    public ResponseEntity<GetStatsResponseDto> getStats(@RequestParam String username, Authentication authentication) throws CisException {
        Token token = (Token) authentication;
        return ResponseEntity.ok(statsService.getStats(username, token.getAuthenticateResponseDto()));
    }


}
