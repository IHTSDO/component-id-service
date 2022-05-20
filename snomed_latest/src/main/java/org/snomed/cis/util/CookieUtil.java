package org.snomed.cis.util;

import org.snomed.cis.exception.CisException;
import org.snomed.cis.pojo.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CookieUtil {

    @Autowired
    private Config config;

    public String fetchTokenCookieValue(ResponseEntity<String> response) throws CisException {
        String tokenCookie = fetchTokenCookie(response);
        String cookieKey = config.getIms().getCookieName() + "=";
        String tokenCookieValue = fetchValue(tokenCookie, cookieKey);
        return tokenCookieValue;
    }

    public String fetchTokenCookie(ResponseEntity<String> response) throws CisException {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null) {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Set-Cookie header not available");
        }
        String cookieKey = config.getIms().getCookieName() + "=";
        Optional<String> tokenCookieOptional = cookies.stream().filter(cookie -> cookie.contains(cookieKey)).findFirst();
        if (tokenCookieOptional.isEmpty()) {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Token cookie not available");
        }
        String tokenCookie = tokenCookieOptional.get();

        return tokenCookie;
    }

    public String fetchValue(String input, String key) {
        String value = "";
        int startIndex = input.indexOf(key) + key.length();
        value = input.substring(startIndex, input.indexOf(";", startIndex));
        return value;
    }

    public String updateDomain(String tokenCookie, String newDomainValue) {
        String domainKey = "Domain=";
        String domainValue = fetchValue(tokenCookie, domainKey);
        return tokenCookie.replace(domainKey + domainValue, domainKey + newDomainValue);
    }
}
