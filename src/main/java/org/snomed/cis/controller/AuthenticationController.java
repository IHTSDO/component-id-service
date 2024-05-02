package org.snomed.cis.controller;

import io.swagger.annotations.Api;
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
import springfox.documentation.annotations.ApiIgnore;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Api(tags = "Authentication", value = "Authentication")
@RestController
public class AuthenticationController {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletRequest httpRequest) throws CisException {
        return authenticationService.login(loginRequestDto, httpRequest);
    }

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

    @PostMapping("/users/logout")
    public ResponseEntity<EmptyDto> logout(@RequestParam String token, @RequestBody @Valid LogoutRequestDto logoutRequestDto) throws CisException {
        return authenticationService.logout(logoutRequestDto);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponseDto> authenticate(@RequestParam(required = false) String token, @ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(authToken.getAuthenticateResponseDto(), HttpStatus.OK);
    }

}
