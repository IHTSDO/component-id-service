package org.snomed.cis.security;

import org.json.JSONObject;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super("/**");
        this.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        Optional<String> tokenOptional = Optional.empty();
        if (uri.equalsIgnoreCase("/api/authenticate") || uri.equalsIgnoreCase("/api/logout")) {
            String requestBody = "";
            try {
                requestBody = new String(request.getInputStream().readAllBytes());//.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                JSONObject requestBodyJson = new JSONObject(requestBody);
                if (requestBodyJson.has("token")) {
                    tokenOptional = Optional.ofNullable(requestBodyJson.getString("token"));
                }
            } catch (IOException e) {
                tokenOptional = Optional.empty();
            }
        } else {
            tokenOptional = Optional.ofNullable(request.getParameter("token"));
        }
        Token authToken = tokenOptional.map(Token::new).orElse(new Token());

        return getAuthenticationManager().authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult)
            throws IOException, ServletException {

        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

}
