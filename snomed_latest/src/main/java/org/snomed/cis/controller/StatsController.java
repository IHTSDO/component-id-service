package org.snomed.cis.controller;

import io.swagger.annotations.Api;
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

@Api(tags = "Stats", value = "Stats")
@RestController
public class StatsController {
    private final Logger logger = LoggerFactory.getLogger(StatsController.class);
    @Autowired
    StatsService statsService;


    @GetMapping("/stats")
    public ResponseEntity<GetStatsResponseDto> getStats(@RequestParam String username, Authentication authentication) throws CisException {
        Token token = (Token) authentication;
        logger.info("Request received for - username :: {} - authenticateResponseDto :: {}", username,token.getAuthenticateResponseDto());
        return ResponseEntity.ok(statsService.getStats(username, token.getAuthenticateResponseDto()));
    }


}
