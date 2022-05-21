package org.snomed.cis.controller;

import org.snomed.cis.controller.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletRequest httpRequest) throws CisException {
        return authenticationService.login(loginRequestDto, httpRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<EmptyDto> logout(@RequestBody @Valid LogoutRequestDto logoutRequestDto) throws CisException {
        return authenticationService.logout(logoutRequestDto);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponseDto> authenticate(@RequestBody @Valid AuthenticateRequestDto authenticateRequestDto) throws CisException {
        return authenticationService.authenticate(authenticateRequestDto);
    }
    
}
