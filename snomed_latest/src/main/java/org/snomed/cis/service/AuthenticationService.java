package org.snomed.cis.service;

import org.json.JSONObject;
import org.snomed.cis.controller.dto.LoginRequestDto;
import org.snomed.cis.controller.dto.LoginResponseDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.pojo.Config;
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

    @Autowired
    private Config config;

    @Autowired
    private RequestManager requestManager;

    @Autowired
    private CookieUtil cookieUtil;

    public ResponseEntity<LoginResponseDto> login(LoginRequestDto loginRequestDto, HttpServletRequest httpRequest) throws CisException {
        //authenticate with IMS
        String url = config.getIms().getUrl().getBase() + config.getIms().getUrl().getLogin();
        JSONObject payload = new JSONObject();
        payload.put("login", loginRequestDto.getUsername());
        payload.put("password", loginRequestDto.getPassword());
        ResponseEntity<String> imsResponse = requestManager.postRequest(url, null, payload.toString());

        //fetch cookie value, cookie string with updated domain
        String token = cookieUtil.fetchTokenCookieValue(imsResponse);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.updateDomain(cookieUtil.fetchTokenCookie(imsResponse), httpRequest.getServerName()));

        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(LoginResponseDto.builder().token(token).build());
    }

}
