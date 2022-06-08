package org.snomed.cis.security;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private String contextPath = "/api";

    /*
    Map of public endpoints
    <"/sct/namespaces",[GET]>
    */
    private Map<String, List<HttpMethod>> publicEndpointsMap = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>(contextPath + "/sct/namespaces", Stream.of(HttpMethod.GET)
                            .collect(Collectors.toList())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super("/**");
        this.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        Optional<String> tokenOptional = Optional.empty();
        boolean isPublicEndpoint = isPublicEndpointRequest(request);
        if (uri.equalsIgnoreCase("/api/authenticate") || uri.equalsIgnoreCase("/api/users/logout")) {
            String requestBody = "";
            try {
                requestBody = new String(request.getInputStream().readAllBytes());//.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                JSONObject requestBodyJson = new JSONObject(requestBody);
                if (requestBodyJson.has("token")) {
                    tokenOptional = Optional.ofNullable(requestBodyJson.getString("token"));
                }
            } catch (IOException e) {
                tokenOptional = Optional.empty();
            } catch (JSONException e) { //check if token is avaialble as form data
                List<NameValuePair> formEntityList = URLEncodedUtils.parse(requestBody, StandardCharsets.UTF_8);
                for (NameValuePair entry : formEntityList) {
                    if ("token".equalsIgnoreCase(entry.getName()))
                        tokenOptional = Optional.ofNullable(entry.getValue());
                }
                if (tokenOptional.isEmpty()) {
                    tokenOptional = Optional.ofNullable(request.getParameter("token"));
                }
            }
        } else {
            tokenOptional = Optional.ofNullable(request.getParameter("token"));
        }
        Token authToken = tokenOptional.map(s -> new Token(s, isPublicEndpoint)).orElseGet(() -> new Token(isPublicEndpoint));
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

    private boolean isPublicEndpointRequest(HttpServletRequest request) {
        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        boolean isPublicEndpointRequest = false;

        if (publicEndpointsMap.containsKey(requestUri)) {
            List<HttpMethod> methods = publicEndpointsMap.get(requestUri);
            isPublicEndpointRequest = methods.stream().anyMatch(m -> m.toString().equalsIgnoreCase(requestMethod));
        }
        return isPublicEndpointRequest;
    }

}
