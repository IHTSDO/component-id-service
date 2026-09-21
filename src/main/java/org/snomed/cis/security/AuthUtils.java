package org.snomed.cis.security;

import jakarta.servlet.http.HttpServletRequest;
import org.snomed.cis.dto.AuthenticateResponseDto;
import org.snomed.cis.exception.CisException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static Token getAuthToken(Authentication authentication) throws CisException {
        if (authentication != null) {
            return resolveFromAuthentication(authentication);
        }

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            Token token = resolveFromRequest(servletAttrs.getRequest());
            if (token != null) {
                return token;
            }
        }

        Token contextToken = resolveFromSecurityContextHolder();
        if (contextToken != null) {
            return contextToken;
        }

        throw new CisException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    private static Token resolveFromAuthentication(Authentication authentication) throws CisException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (authentication instanceof Token authToken) {
            return authToken;
        }
        if (authentication.isAuthenticated()) {
            return convertToToken(authentication);
        }
        throw new ClassCastException("Cannot cast " + authentication.getClass().getName() + " to Token");
    }

    private static Token resolveFromRequest(HttpServletRequest req) {
        Token token = findTokenInRequest(req);
        if (token != null) {
            return token;
        }
        Authentication auth = findAuthInRequest(req);
        if (auth != null) {
            return convertToToken(auth);
        }
        return null;
    }

    private static Token findTokenInRequest(HttpServletRequest req) {
        Enumeration<String> names = req.getAttributeNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            Object val = req.getAttribute(name);
            SecurityContext sc = extractSecurityContext(val);
            if (sc != null && sc.getAuthentication() instanceof Token authToken) {
                return authToken;
            }
            if (val instanceof Token authToken) {
                return authToken;
            }
        }
        if (req.getUserPrincipal() instanceof Token authToken) {
            return authToken;
        }
        return null;
    }

    private static Authentication findAuthInRequest(HttpServletRequest req) {
        Enumeration<String> names = req.getAttributeNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            Object val = req.getAttribute(name);
            SecurityContext sc = extractSecurityContext(val);
            if (sc != null) {
                Authentication auth = sc.getAuthentication();
                if (isValidAuth(auth)) {
                    return auth;
                }
            }
        }
        if (req.getUserPrincipal() instanceof Authentication auth && isValidAuth(auth)) {
            return auth;
        }
        return null;
    }

    private static boolean isValidAuth(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private static Token resolveFromSecurityContextHolder() {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth instanceof Token authToken) {
            return authToken;
        }
        if (isValidAuth(currentAuth)) {
            return convertToToken(currentAuth);
        }
        return null;
    }

    private static SecurityContext extractSecurityContext(Object val) {
        if (val instanceof SecurityContext sc) {
            return sc;
        }
        if (val instanceof Supplier<?> supplier) {
            Object supplied = supplier.get();
            if (supplied instanceof SecurityContext sc) {
                return sc;
            }
        }
        return null;
    }

    private static Token convertToToken(Authentication auth) {
        List<GrantedAuthority> authorities = auth.getAuthorities() != null
                ? new ArrayList<>(auth.getAuthorities())
                : new ArrayList<>();
        List<String> roleStrings = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        AuthenticateResponseDto authDto = AuthenticateResponseDto.builder()
                .name(auth.getName())
                .roles(roleStrings)
                .build();
        return new Token(auth.getName(), auth.getName(), true, authorities, authDto);
    }
}
