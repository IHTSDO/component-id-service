package org.snomed.cis.security;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String TOKEN = "token";

    private String contextPath = "/api";

    private String imsCookieName;

    /*
    Map of public endpoints
    <"/sct/namespaces",[GET]>
    */
    private Map<String, List<HttpMethod>> publicEndpointsMap = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>(contextPath + "/sct/namespaces", Stream.of(HttpMethod.GET)
                            .collect(Collectors.toList())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


    public TokenAuthenticationFilter(AuthenticationManager authenticationManager, String imsCookieName) {
        super("/**");
        this.setAuthenticationManager(authenticationManager);
        this.imsCookieName = imsCookieName;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response) {
        Optional<String> tokenOptional = extractToken(request);
        boolean isPublicEndpoint = isPublicEndpointRequest(request);
        Token authToken = tokenOptional
                .map(s -> new Token(s, isPublicEndpoint))
                .orElseGet(() -> new Token(isPublicEndpoint));
        return getAuthenticationManager().authenticate(authToken);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.endsWith("/authenticate") || uri.endsWith("/users/logout")) {
            return extractTokenFromAuthRequest(request);
        } else if (uri.endsWith("/sct/namespaces")) {
            return extractTokenFromCookieHeader(request.getHeader("cookie"));
        } else {
            return Optional.ofNullable(request.getParameter(TOKEN));
        }
    }

    private Optional<String> extractTokenFromAuthRequest(HttpServletRequest request) {
        String requestBody = "";
        try {
            requestBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject requestBodyJson = new JSONObject(requestBody);
            if (requestBodyJson.has(TOKEN)) {
                return Optional.ofNullable(requestBodyJson.getString(TOKEN));
            }
        } catch (IOException e) {
            return Optional.empty();
        } catch (JSONException e) {
            Optional<String> formToken = extractTokenFromFormData(requestBody);
            if (formToken.isPresent()) {
                return formToken;
            }
        }
        return Optional.ofNullable(request.getParameter(TOKEN));
    }

    private Optional<String> extractTokenFromFormData(String requestBody) {
        if (!requestBody.isBlank()) {
            for (String pair : requestBody.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length > 0 && TOKEN.equalsIgnoreCase(URLDecoder.decode(parts[0], StandardCharsets.UTF_8))) {
                    if (parts.length > 1) {
                        return Optional.ofNullable(URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                    }
                    break;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractTokenFromCookieHeader(String cookieHeaderValue) {
        if (cookieHeaderValue == null) {
            return Optional.empty();
        }
        Optional<String> tsAuthorToken = extractTokenFromTsAuthorCookie(cookieHeaderValue);
        if (tsAuthorToken.isPresent()) {
            return tsAuthorToken;
        }
        return extractTokenFromImsCookie(cookieHeaderValue);
    }

    private Optional<String> extractTokenFromTsAuthorCookie(String cookieHeaderValue) {
        Optional<String> tsAuthorCookieStringOpt = Arrays.stream(cookieHeaderValue.split(";"))
                .filter(c -> c.contains("ts-author"))
                .findAny();
        if (tsAuthorCookieStringOpt.isPresent()) {
            String cookieValue = tsAuthorCookieStringOpt.get().substring(tsAuthorCookieStringOpt.get().indexOf("=") + 1);
            try {
                JSONObject cookieValueJsonObj = new JSONObject(cookieValue);
                if (cookieValueJsonObj.has(TOKEN)) {
                    return Optional.ofNullable(cookieValueJsonObj.getString(TOKEN));
                }
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractTokenFromImsCookie(String cookieHeaderValue) {
        Optional<String> imsOpt = Arrays.stream(cookieHeaderValue.split(";"))
                .map(String::trim)
                .filter(c -> c.startsWith(getImsCookieName() + "="))
                .findFirst();
        if (imsOpt.isPresent()) {
            String token = imsOpt.get().substring(imsOpt.get().indexOf("=") + 1);
            if (!token.isBlank()) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
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

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        SecurityContextHolder.clearContext();
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("message",exception.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getOutputStream().println(errorResponse.toString());
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

    private String getImsCookieName() {
        return imsCookieName;
    }


}
