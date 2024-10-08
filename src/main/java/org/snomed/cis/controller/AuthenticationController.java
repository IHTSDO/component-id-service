package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        String formStr = "";
        try {
            formStr = new String(httpRequest.getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new CisException(HttpStatus.BAD_REQUEST, "invalid form data submitted");
        }
        List<NameValuePair> formEntityList = URLEncodedUtils.parse(formStr, StandardCharsets.UTF_8);
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        for (NameValuePair entry : formEntityList) {
            if ("username".equalsIgnoreCase(entry.getName()))
                loginRequestDto.setUsername(entry.getValue());
            else if ("password".equalsIgnoreCase(entry.getName()))
                loginRequestDto.setPassword(entry.getValue());
        }
        ValidationUtil.validateLoginRequestDto(loginRequestDto);
        return authenticationService.login(loginRequestDto, httpRequest);
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
