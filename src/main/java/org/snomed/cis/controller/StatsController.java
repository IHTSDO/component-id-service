package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.dto.GetStatsResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "Stats" , description = "Stats Controller")
@RestController
public class StatsController {
    private final Logger logger = LoggerFactory.getLogger(StatsController.class);
    @Autowired
    StatsService statsService;

    @GetMapping("/stats")
    public ResponseEntity<GetStatsResponseDto> getStats(@RequestParam String token, @RequestParam String username, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(statsService.getStats(username, authToken.getAuthenticateResponseDto()));
    }


}
