package org.snomed.cis.security;

import org.snomed.cis.dto.AuthenticateRequestDto;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.exception.CisException;
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

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Token authToken = (Token)authentication;
        String token = (String) authentication.getPrincipal();
        if(authToken.getIsPublicEndpoint() && token == null){
            return authToken;
        }
        else if (token == null) {
            throw new AuthenticationCredentialsNotFoundException("token not provided");
        }
        ResponseEntity<AuthenticateResponseDto> authenticateResponse;
        try {
            authenticateResponse = authenticationService.authenticate(new AuthenticateRequestDto(token));
        } catch (CisException e) {
            if (HttpStatus.INTERNAL_SERVER_ERROR.equals(e.getStatus())) {
                throw new AuthenticationServiceException(e.getErrorMessage());
            }
            else {
                throw new BadCredentialsException(e.getErrorMessage());
            }
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