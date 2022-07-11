package org.snomed.cis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.config.ImsConfig;
import org.snomed.cis.dto.*;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.util.CookieUtil;
import org.snomed.cis.util.RequestManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuthenticationService {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    @Autowired
    private ImsConfig imsConfig;

    @Autowired
    private RequestManager requestManager;

    @Autowired
    private CookieUtil cookieUtil;

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto loginRequestDto, HttpServletRequest httpRequest) throws CisException {
        //authenticate with IMS
        String url = imsConfig.getBaseUrl() + imsConfig.getLoginUrl();
        JSONObject payload = new JSONObject();
        payload.put("login", loginRequestDto.getUsername());
        payload.put("password", loginRequestDto.getPassword());
        ResponseEntity<String> imsResponse;
        try {
            imsResponse = requestManager.postRequest(url, null, payload.toString());
        } catch (CisException e) {
            if (e.getStatus().is4xxClientError()) {
                logger.error("call to IMS returned 4xx while trying to login for user '{}'", loginRequestDto.getUsername(), e);
                throw new CisException(HttpStatus.UNAUTHORIZED, "username/password incorrect for input user '"+loginRequestDto.getPassword()+"'");
            } else if (e.getStatus().is5xxServerError()) {
                logger.error("call to IMS returned 5xx while trying to login for user '{}'", loginRequestDto.getUsername(), e);
                throw new CisException(e.getStatus(), "unknown error");
            } else {
                logger.error("call to IMS returned unknown error while trying to login for user '{}'", loginRequestDto.getUsername(), e);
                throw new CisException(e.getStatus(), "unknown error");
            }
        }
        logger.info("user '"+loginRequestDto.getUsername()+"'successfully logged in");

        //token response value
        String token = cookieUtil.fetchTokenCookieValue(imsResponse);

        //set token value in cookie - Uncomment below block if set-cookie header has to be set in response
        /*
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.updateDomain(cookieUtil.fetchTokenCookie(imsResponse), httpRequest.getServerName()));
         */

        return ResponseEntity.status(HttpStatus.OK)/*.headers(responseHeaders)*/.body(LoginResponseDto.builder().token(token).build());
    }

    public ResponseEntity<EmptyDto> logout(LogoutRequestDto logoutRequestDto) throws CisException {
        String token = logoutRequestDto.getToken();
        String url = imsConfig.getBaseUrl() + imsConfig.getLogoutUrl();

        requestManager.postRequestWithoutPayload(url, getHttpHeaders(token));

        return new ResponseEntity<>(new EmptyDto(), HttpStatus.OK);
    }

    public ResponseEntity<AuthenticateResponseDto> authenticate(AuthenticateRequestDto authenticateRequestDto) throws CisException {
        String token = authenticateRequestDto.getToken();
        String url = imsConfig.getBaseUrl() + imsConfig.getAuthenticateUrl();

        ResponseEntity<String> imsResponse;
        try {
            imsResponse = requestManager.getRequest(url, getHttpHeaders(token));
        } catch (CisException e) {
            if (e.getStatus().is4xxClientError()) {
                logger.error("call to IMS returned 4xx error while trying to authenticate", e);
                throw new CisException(HttpStatus.UNAUTHORIZED, "invalid token");
            } else if (e.getStatus().is5xxServerError()) {
                logger.error("call to IMS returned 5xx error while trying to authenticate", e);
                throw new CisException(e.getStatus(), "unknown error");
            } else {
                logger.error("call to IMS returned unknown error while trying to authenticate", e);
                throw new CisException(e.getStatus(), "unknown error");
            }
        }

        ImsGetAccountResponseDto imsGetAccountResponseDto = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            imsGetAccountResponseDto = objectMapper.readValue(imsResponse.getBody(), ImsGetAccountResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new CisException(HttpStatus.INTERNAL_SERVER_ERROR, "error while parsing ims get account response");
        }

        AuthenticateResponseDto authenticateResponseDto = AuthenticateResponseDto.builder()
                .email(imsGetAccountResponseDto.getEmail())
                .name(imsGetAccountResponseDto.getLogin())
                .displayName(imsGetAccountResponseDto.getLogin())
                .firstName(imsGetAccountResponseDto.getFirstName())
                .lastName(imsGetAccountResponseDto.getLastName())
                .langKey(imsGetAccountResponseDto.getLangKey())
                .roles(imsGetAccountResponseDto.getRoles())
                .build();
        return new ResponseEntity<>(authenticateResponseDto, HttpStatus.OK);
    }

    private HttpHeaders getHttpHeaders(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Cookie", imsConfig.getCookieName() + "=" + token);
        return httpHeaders;
    }

}
