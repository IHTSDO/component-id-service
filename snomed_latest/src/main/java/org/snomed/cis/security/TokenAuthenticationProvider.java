package org.snomed.cis.security;

import org.snomed.cis.pojo.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
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

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String token = (String) authentication.getPrincipal();

        if("tPy_xQFRWN7Cw6Ra3BuhPwAAAAAAAIACa2VlcnRoaWth".equals(token)){
            List<GrantedAuthority> authorities = new LinkedList<>();
            authorities.add(new SimpleGrantedAuthority("USER"));
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
            return new Token(token, "ABCD", true, authorities);
        }else{
            throw new BadCredentialsException("API Key is invalid");
        }
        /*
        if (ObjectUtils.isEmpty(apiKey)) {
            throw new InsufficientAuthenticationException("No API key in request");
        } else {
            if (config.getApiKey().getAdmin().equals(apiKey)) {
                List<GrantedAuthority> authorities = new LinkedList<>();
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
                return new ApiKeyAuthenticationToken(apiKey, -1L, true, authorities);
            }
            Optional<ApiKeys> apiKeysOptional = apiKeysRepository.findFirstByApiKey(apiKey);
            if (apiKeysOptional.isPresent()) {
                return new ApiKeyAuthenticationToken(apiKey, apiKeysOptional.get().getUser().getId(), true, AuthorityUtils.NO_AUTHORITIES);
            }
            throw new BadCredentialsException("API Key is invalid");
        }
        */
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return Token.class.isAssignableFrom(authentication);
    }
}