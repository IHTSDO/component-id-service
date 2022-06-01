package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.dto.EmptyDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.AuthorizationService;
import org.snomed.cis.service.NamespaceService;
import org.snomed.cis.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(tags = "Authorization", value = "Authorization")
@RestController
public class AuthorizationController {
    private final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);
    @Autowired
    NamespaceService namespaceService;

    @Autowired
    SchemeService schemeService;

    @Autowired
    private AuthorizationService authorizationService;


    @GetMapping("/users")
    public ResponseEntity<List<String>> getUsers(@RequestParam String token) {
        return new ResponseEntity<>(authorizationService.getUsers(), HttpStatus.OK);
    }

    @GetMapping("/users/{username}/groups")
    public ResponseEntity<List<String>> getUserGroups(@RequestParam String token, @ApiIgnore Authentication authentication) {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(authorizationService.getUserGroups(authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @DeleteMapping("/users/{username}/groups/{groupName}")
    public ResponseEntity<EmptyDto> deleteMember(@RequestParam String token, @PathVariable String groupName, @ApiIgnore Authentication authentication) {
        Token authToken = (Token) authentication;
        authorizationService.deleteMember(authToken.getUserName(), groupName);
        return new ResponseEntity<>(new EmptyDto(), HttpStatus.OK);
    }

    @PostMapping("/users/{username}/groups/{groupName}")
    public ResponseEntity<EmptyDto> addMember(@RequestParam String token, @PathVariable String groupName, @ApiIgnore Authentication authentication) {
        Token authToken = (Token) authentication;
        authorizationService.addMember(authToken.getUserName(), groupName);
        return new ResponseEntity<>(new EmptyDto(), HttpStatus.OK);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<String>> getGroups(@RequestParam String token) throws CisException {
        return new ResponseEntity<>(authorizationService.getGroups(), HttpStatus.OK);
    }

    @GetMapping("/groups/{groupName}/users")
    public ResponseEntity<List<String>> getGroupUsers(@RequestParam String token, @PathVariable String groupName) {
        return new ResponseEntity<>(authorizationService.getGroupUsers(groupName), HttpStatus.OK);
    }

    @GetMapping(value = "/sct/namespaces/{namespaceId}/permissions")
    public ResponseEntity<List<PermissionsNamespace>> getNamespacePermissions(@RequestParam String token, @PathVariable String namespaceId) throws CisException {
        return new ResponseEntity<>(namespaceService.getNamespacePermissions(namespaceId), HttpStatus.OK);
    }

    @DeleteMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    public ResponseEntity<String> deleteNamespacePermissionsOfUser(@RequestParam String token, @PathVariable String namespaceId, @ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(namespaceService.deleteNamespacePermissionsOfUser(namespaceId, authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @PostMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    public ResponseEntity<String> createNamespacePermissionsOfUser(@RequestParam String token, @PathVariable String namespaceId, @RequestParam String role, @ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(namespaceService.createNamespacePermissionsOfUser(namespaceId, authToken.getUserName(), role, authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @GetMapping(value = "/schemes/{schemeName}/permissions")
    public ResponseEntity<List<PermissionsScheme>> getPermissionsForScheme(@RequestParam String token, @PathVariable String schemeName) throws CisException {
        return new ResponseEntity<>(schemeService.getPermissionsForScheme(schemeName), HttpStatus.OK);
    }


    @DeleteMapping("/schemes/{schemeName}/permissions/{username}")
    public ResponseEntity<String> deleteSchemePermissions(@RequestParam String token, @PathVariable String schemeName, @PathVariable String username, @ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(schemeService.deleteSchemePermissions(schemeName, username, authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @PostMapping("/schemes/{schemeName}/permissions/{username}")
    public ResponseEntity<String> createSchemePermissions(@RequestParam String token, @PathVariable String schemeName, @RequestParam String role, @ApiIgnore Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(schemeService.createSchemePermissions(schemeName, role, authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

}
