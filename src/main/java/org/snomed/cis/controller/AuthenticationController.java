package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.AuthenticationService;
import org.snomed.cis.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "Authentication" , description = "Authentication Controller")
@RestController
public class AuthenticationController {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    @Autowired
    private AuthenticationService authenticationService;

    @Operation(summary = "login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletRequest httpRequest) throws CisException {
        return authenticationService.login(loginRequestDto, httpRequest);
    }

    @Operation(summary = "loginUI")
    @PostMapping(path = "/loginUI", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<LoginResponseDto> loginUI(HttpServletRequest httpRequest) throws CisException {
        String formStr = readFormData(httpRequest);
        LoginRequestDto loginRequestDto = parseLoginRequest(formStr);
        ValidationUtil.validateLoginRequestDto(loginRequestDto);
        return authenticationService.login(loginRequestDto, httpRequest);
    }

    private String readFormData(HttpServletRequest httpRequest) throws CisException {
        try {
            return new String(httpRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CisException(HttpStatus.BAD_REQUEST, "invalid form data submitted");
        }
    }

    private LoginRequestDto parseLoginRequest(String formStr) {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        if (formStr.isBlank()) {
            return loginRequestDto;
        }
        for (String pair : formStr.split("&")) {
            parseFormPair(pair, loginRequestDto);
        }
        return loginRequestDto;
    }

    private void parseFormPair(String pair, LoginRequestDto loginRequestDto) {
        String[] parts = pair.split("=", 2);
        if (parts.length == 0) {
            return;
        }
        String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
        String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
        if ("username".equalsIgnoreCase(key)) {
            loginRequestDto.setUsername(value);
        } else if ("password".equalsIgnoreCase(key)) {
            loginRequestDto.setPassword(value);
        }
    }

    @Operation(summary = "logout")
    @PostMapping("/users/logout")
    public ResponseEntity<EmptyDto> logout(@RequestParam String token, @RequestBody @Valid LogoutRequestDto logoutRequestDto) throws CisException {
        return authenticationService.logout(logoutRequestDto);
    }

    @Operation(summary = "authenticate")
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponseDto> authenticate(@RequestParam(required = false) String token, @Parameter(hidden = true) Authentication authentication) {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(authToken.getAuthenticateResponseDto(), HttpStatus.OK);
    }

}
