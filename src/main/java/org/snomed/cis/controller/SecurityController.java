package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.config.ImsConfig;
import org.snomed.cis.dto.UserDTO;
import org.snomed.cis.exception.CisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

//@Api(tags = "Authentication", value = "Authentication")
//This class is under modification, code is under Draft version. Null or empty checks will be handled in next version.
@RestController
public class SecurityController {
    private final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    @Autowired
    private HttpServletRequest servReq;

    @Autowired
    private HttpServletResponse servletResponse;

    @Autowired
    private ImsConfig imsConfig;

    @Operation(
            summary = "Authentication & Authorization",
            description = "Returns authentication & authorization operations"
                    + "<p>The following properties can be expanded:"
                    + "<p>"
                    + "&bull;  &ndash; the list of descendants of the concept<br>", tags = {"Authentication & Authorization"})
    @ApiResponses({
            // @ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
            // @ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
            // @ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
    })





   /* @PostMapping("/authenticate")
    @ResponseBody
    public UserDTO getUserFromToken(@RequestBody String token) throws CisException {
        return this.authenticate();
    }*/


    public List<String> getUserGroup(@PathVariable String username, @RequestParam(name = "token", required = false) String token) throws CisException {
        UserDTO user = new UserDTO();
        List<String> role = new ArrayList<>();
        ResponseEntity<UserDTO> resp = this.isValidUser(servReq);
        if (resp.hasBody())
            user = resp.getBody();
        //role.add(user.getRoles().get(0).split("_")[1]);
        for (String roleGroup :
                user.getRoles()) {
            role.add(roleGroup.split("_")[1]);
        }
        return role;
    }

    public List<String> getUserGroupWithoutToken(@PathVariable String username) throws CisException {
        UserDTO user = new UserDTO();
        List<String> role = new ArrayList<>();
        ResponseEntity<UserDTO> resp = this.isValidUser(servReq);
        if (resp.hasBody())
            user = resp.getBody();
        //role.add(user.getRoles().get(0).split("_")[1]);
        for (String roleGroup :
                user.getRoles()) {
            role.add(roleGroup.split("_")[1]);
        }
        return role;
    }

    public boolean validateUserToken(String userToken) {
        Cookie[] cookieAr = servReq.getCookies();
        Cookie cookie = cookieAr[0];
        if (userToken.equals(cookie.getValue()))
            return true;
        else
            return false;
    }

    public UserDTO authenticate() throws CisException {
        ResponseEntity<UserDTO> resp = this.isValidUser(servReq);
        if (resp.hasBody())
            return resp.getBody();
        else
            return null;
    }

    //@PostMapping("/api/login")
    /*@RequestMapping(value = "/loginUI", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    LoginResponseDto loginFromUI(LoginCredentialsDto credentialsDto) throws JsonProcessingException, CisException {
        return this.validateUser(credentialsDto, servletResponse);
    }*/

   /* @Cacheable(value = "token-cache")
    @GetMapping("/login")
    public @ResponseBody
    LoginResponseDto validateUser(LoginCredentialsDto credentialsDto, HttpServletResponse resp) throws JsonProcessingException, CisException {
        LoginResponseDto responseDto = new LoginResponseDto();
        String uri = config.getIms().getUrl().getBase() + config.getIms().getUrl().getAuthenticate();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserDTO dto = new UserDTO(credentialsDto.getUsername(), credentialsDto.getPassword(), "", "", "", "", new ArrayList<>());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String userAsJson = ow.writeValueAsString(dto);
        HttpEntity<?> request = new HttpEntity<>(userAsJson, headers);
        ResponseEntity response;
        try {
            response = restTemplate.exchange(uri, HttpMethod.POST, request, ResponseEntity.class);
        } catch (Exception e) {
            throw new CisException(HttpStatus.UNAUTHORIZED, "Account with name <" + credentialsDto.getUsername() + "> failed to authenticate: User <" + credentialsDto.getUsername() + "> does not exist");
        }
        System.out.println("cookies:" + response.getHeaders().get("set-cookie"));
        String[] cookieArr = response.getHeaders().get("set-cookie").get(0).split(";");
        String[] arr = cookieArr[0].split(";")[0].split("=");
        Cookie cookie = new Cookie(arr[0], arr[1]);
        resp.addCookie(cookie);
        responseDto.setToken(response.getHeaders().get("set-cookie").get(0).split(";")[0].split("=")[1]);
        return responseDto;
    }*/

    /*@Cacheable(value="token-cache")
    @GetMapping("/login")
    public @ResponseBody LoginResponseDto validateUser(HttpServletResponse resp
    ) throws JsonProcessingException {
        LoginResponseDto responseDto = new LoginResponseDto();
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
        responseDto.setToken(response.getHeaders().get("set-cookie").get(0).split(";")[0].split("=")[1]);
        return responseDto;
    }*/

    @Operation(summary = "isValidUser")
    @CacheEvict(value = "account-cache", key = "'AccountCache'+#request.getCookies()[0]", condition = "request.getCookies()[0]==''")
    @Cacheable(value = "account-cache", key = "'AccountCache'+#request.getCookies()[0]", condition = "request.getCookies()[0]!=''")
    @GetMapping("/isValidUser")
    @ResponseBody
    public ResponseEntity<UserDTO> isValidUser(HttpServletRequest request) throws CisException {
        String uri = imsConfig.getBaseUrl() + imsConfig.getAuthenticateUrl();
        Cookie[] cookieAr = request.getCookies();
        Cookie cookie = cookieAr[0];
        if (cookie.getValue().isEmpty() || cookie.getValue().isBlank()) {
            throw new CisException(HttpStatus.ACCEPTED, "Authorization Token Expired or User Logged Out");
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        //Cookie cookie = new Cookie("dev-ims-ihtsdo","rem-4YIi85oWY5Ab6szkmwAAAAAAAIACa2VlcnRoaWth");
        headers.add(HttpHeaders.COOKIE, cookie.getName() + "=" + cookie.getValue());
        HttpEntity<?> req = new HttpEntity<>(headers);
        ResponseEntity<UserDTO> response = restTemplate.exchange(uri, HttpMethod.GET, req, UserDTO.class);
        return response;
    }

    @Operation(summary = "logoutUser")
    @GetMapping("/logout")
    @ResponseBody
    public String logoutUser(HttpServletRequest request, HttpServletResponse logoutResp) {
        String uri = imsConfig.getBaseUrl() + imsConfig.getLogoutUrl();
        Cookie[] logoutCookieAr = request.getCookies();
        Cookie logoutCookie = logoutCookieAr[0];
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        //Cookie cookie = new Cookie("dev-ims-ihtsdo","rem-4YIi85oWY5Ab6szkmwAAAAAAAIACa2VlcnRoaWth");
        headers.add(HttpHeaders.COOKIE, logoutCookie.getName() + "=" + logoutCookie.getValue());
        HttpEntity<?> req = new HttpEntity<>(headers);
        ResponseEntity response = restTemplate.exchange(uri, HttpMethod.POST, req, ResponseEntity.class);
        String[] logoutArr = response.getHeaders().get("set-cookie").get(0).split(";")[0].split("=");
        Cookie logoutRespCookie = null;
        if (logoutArr.length > 1)
            logoutRespCookie = new Cookie(logoutArr[0], logoutArr[1]);
        else if (logoutArr.length == 1)
            logoutRespCookie = new Cookie(logoutArr[0], "");
        logoutResp.addCookie(logoutRespCookie);
        return response.getHeaders().get("set-cookie").get(0);
    }

}
