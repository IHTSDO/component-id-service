package org.snomed.cis.security;


import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

@Transient
public class Token extends AbstractAuthenticationToken {

    private String token;

    private String userName;

    public Token(String token, String userName, boolean authenticated, List<GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.userName = userName;
        setAuthenticated(authenticated);
    }

    public Token(String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
        setAuthenticated(false);
    }

    public Token() {
        super(AuthorityUtils.NO_AUTHORITIES);
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    public String getToken() {
        return token;
    }

    public String getUserName() {
        return userName;
    }
}