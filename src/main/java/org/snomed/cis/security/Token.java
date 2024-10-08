package org.snomed.cis.security;


import lombok.Getter;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

@Transient
@Getter
public class Token extends AbstractAuthenticationToken {

    private String token;

    private String userName;

    private AuthenticateResponseDto authenticateResponseDto;

    private Boolean isPublicEndpoint;

    public Token(String token, String userName, boolean authenticated, List<GrantedAuthority> authorities, AuthenticateResponseDto authenticateResponseDto) {
        super(authorities);
        this.token = token;
        this.userName = userName;
        this.authenticateResponseDto = authenticateResponseDto;
        setAuthenticated(authenticated);
    }

    public Token(String token, boolean isPublicEndpoint) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
        setAuthenticated(false);
        this.isPublicEndpoint = isPublicEndpoint;
    }

    public Token(boolean isPublicEndpoint) {
        super(AuthorityUtils.NO_AUTHORITIES);
        setAuthenticated(false);
        this.isPublicEndpoint = isPublicEndpoint;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

}