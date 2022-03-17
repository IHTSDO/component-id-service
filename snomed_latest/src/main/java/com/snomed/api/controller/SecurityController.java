package com.snomed.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.snomed.api.config.UserProperties;
import com.snomed.api.controller.dto.UserDTO;
import com.snomed.api.exception.APIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.*;
import java.util.*;

//This class is under modification, code is under Draft version. Null or empty checks will be handled in next version.
@RestController
public class SecurityController {

    @Autowired
    private HttpServletRequest servReq;

    @Autowired
    private UserProperties userProperties;

    @Value("${snomed.devIms.authenticate}")
    String authenticateUrl;

    @Value("${snomed.devIms.logout}")
    String logoutUrl;

    @Value("${snomed.devIms.account}")
    String accountUrl;

    public UserDTO authenticate() throws APIException {
        ResponseEntity<UserDTO> resp = this.isValidUser(servReq);
        if(resp.hasBody())
            return resp.getBody();
        else
            return null;
    }

    @Cacheable(value="token-cache")
    @GetMapping("/login")
    public String validateUser(HttpServletResponse resp
    ) throws JsonProcessingException {
        String uri = authenticateUrl;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        System.out.println("roles from Prop:"+userProperties.getRoles().get(0));
        UserDTO dto = new UserDTO(userProperties.getLogin(),userProperties.getPassword(),userProperties.getFirstName(),userProperties.getLastName(),userProperties.getEmail(),userProperties.getLangKey(), userProperties.getRoles());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String userAsJson = ow.writeValueAsString(dto);
        HttpEntity<?> request = new HttpEntity<>(userAsJson,headers);
        ResponseEntity response = restTemplate.exchange(uri, HttpMethod.POST,request, ResponseEntity.class);
        System.out.println("cookies:"+response.getHeaders().get("set-cookie"));
        String[] cookieArr = response.getHeaders().get("set-cookie").get(0).split(";");
        String[] arr = cookieArr[0].split(";")[0].split("=");
        Cookie cookie= new Cookie(arr[0],arr[1]);
        resp.addCookie(cookie);
        return response.getHeaders().get("set-cookie").get(0).split(";")[0];
    }

    @CacheEvict(value="account-cache", key="'AccountCache'+#request.getCookies()[0]",condition = "request.getCookies()[0]==''")
    @Cacheable(value="account-cache", key="'AccountCache'+#request.getCookies()[0]",condition = "request.getCookies()[0]!=''")
    @GetMapping("/isValidUser")
    @ResponseBody
    public ResponseEntity<UserDTO> isValidUser(HttpServletRequest request) throws APIException {
        String uri = accountUrl;
        Cookie[] cookieAr = request.getCookies();
        Cookie cookie= cookieAr[0];
        System.out.println("Cookie from REQUEST Name:"+cookie.getName());
        System.out.println("Cookie from REQUEST Value:"+cookie.getValue());
        if(cookie.getValue().isEmpty() || cookie.getValue().isBlank())
        {
            throw new APIException(HttpStatus.ACCEPTED,"Authorization Token Expired or User Logged Out");
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        //Cookie cookie = new Cookie("dev-ims-ihtsdo","rem-4YIi85oWY5Ab6szkmwAAAAAAAIACa2VlcnRoaWth");
        headers.add(HttpHeaders.COOKIE, cookie.getName()+"="+cookie.getValue());
        HttpEntity<?> req = new HttpEntity<>(headers);
        System.out.println( "headers from req:"+req.getHeaders());
        ResponseEntity<UserDTO> response = restTemplate.exchange(uri,HttpMethod.GET,req,UserDTO.class);
        return response;
    }

@GetMapping("/logout")
@ResponseBody
    public String logoutUser(HttpServletRequest request,HttpServletResponse logoutResp)
{
    String uri = logoutUrl;
    Cookie[] logoutCookieAr = request.getCookies();
    Cookie logoutCookie= logoutCookieAr[0];
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    //Cookie cookie = new Cookie("dev-ims-ihtsdo","rem-4YIi85oWY5Ab6szkmwAAAAAAAIACa2VlcnRoaWth");
    headers.add(HttpHeaders.COOKIE, logoutCookie.getName()+"="+logoutCookie.getValue());
    HttpEntity<?> req = new HttpEntity<>(headers);
    System.out.println( "headers from req:"+req.getHeaders());
    ResponseEntity response = restTemplate.exchange(uri,HttpMethod.POST,req,ResponseEntity.class);
    String[] logoutArr = response.getHeaders().get("set-cookie").get(0).split(";")[0].split("=");
    Cookie logoutRespCookie = null;
    if(logoutArr.length>1)
        logoutRespCookie = new Cookie(logoutArr[0],logoutArr[1]);
    else if(logoutArr.length == 1)
        logoutRespCookie = new Cookie(logoutArr[0],"");
    logoutResp.addCookie(logoutRespCookie);
    return response.getHeaders().get("set-cookie").get(0);
}
}
