package org.snomed.cis.security;

import org.snomed.cis.controller.dto.AuthenticateRequestDto;
import org.snomed.cis.controller.dto.AuthenticateResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.pojo.Config;
import org.snomed.cis.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private Config config;

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String token = (String) authentication.getPrincipal();

        if (token == null) {
            throw new AuthenticationCredentialsNotFoundException("token not provided");
        }
        ResponseEntity<AuthenticateResponseDto> authenticateResponse;
        try {
            authenticateResponse = authenticationService.authenticate(new AuthenticateRequestDto(token));
        } catch (CisException e) {
            if (HttpStatus.INTERNAL_SERVER_ERROR.equals(e.getStatus())) {
                throw new AuthenticationServiceException(e.getErrorMessage());
            }
            throw new BadCredentialsException(e.getErrorMessage());
        }
        AuthenticateResponseDto authenticateResponseDto = authenticateResponse.getBody();
        List<GrantedAuthority> authorities = new LinkedList<>();
        for (String role : authenticateResponseDto.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return new Token(token, authenticateResponseDto.getName(), true, authorities, authenticateResponseDto);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return Token.class.isAssignableFrom(authentication);
    }
}