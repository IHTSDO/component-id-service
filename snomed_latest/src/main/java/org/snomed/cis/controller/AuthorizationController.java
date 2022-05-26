package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import org.snomed.cis.controller.dto.EmptyDto;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.domain.PermissionsScheme;
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

import java.util.List;

@Api(tags = "Authorization", value = "Authorization")
@RestController
public class AuthorizationController {

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    SchemeService schemeService;

    @Autowired
    private AuthorizationService authorizationService;


    @GetMapping("/users")
    public ResponseEntity<List<String>> getUsers() {
        return new ResponseEntity<>(authorizationService.getUsers(), HttpStatus.OK);
    }

    @GetMapping("/users/{username}/groups")
    public ResponseEntity<List<String>> getUserGroups(Authentication authentication) {
        Token token = (Token) authentication;
        return new ResponseEntity<>(authorizationService.getUserGroups(token.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @DeleteMapping("/users/{username}/groups/{groupName}")
    public ResponseEntity<EmptyDto> deleteMember(@PathVariable String groupName, Authentication authentication) {
        Token token = (Token) authentication;
        authorizationService.deleteMember(token.getUserName(), groupName);
        return new ResponseEntity<>(new EmptyDto(), HttpStatus.OK);
    }

    @PostMapping("/users/{username}/groups/{groupName}")
    public ResponseEntity<EmptyDto> addMember(@PathVariable String groupName, Authentication authentication) {
        Token token = (Token) authentication;
        authorizationService.addMember(token.getUserName(), groupName);
        return new ResponseEntity<>(new EmptyDto(), HttpStatus.OK);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<String>> getGroups() throws CisException {
        return new ResponseEntity<>(authorizationService.getGroups(), HttpStatus.OK);
    }

    @GetMapping("/groups/{groupName}/users")
    public ResponseEntity<List<String>> getGroupUsers(@PathVariable String groupName) {
        return new ResponseEntity<>(authorizationService.getGroupUsers(groupName), HttpStatus.OK);
    }

    @GetMapping(value = "/sct/namespaces/{namespaceId}/permissions")
    public ResponseEntity<List<PermissionsNamespace>> getNamespacePermissions(@PathVariable String namespaceId) throws CisException {
        return new ResponseEntity<>(namespaceService.getNamespacePermissions(namespaceId), HttpStatus.OK);
    }

    @DeleteMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    public ResponseEntity<String> deleteNamespacePermissionsOfUser(@PathVariable String namespaceId, Authentication authentication) throws CisException {
        Token token = (Token) authentication;
        return new ResponseEntity<>(namespaceService.deleteNamespacePermissionsOfUser(namespaceId, token.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @PostMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    public ResponseEntity<String> createNamespacePermissionsOfUser(@PathVariable String namespaceId, @RequestParam String role, Authentication authentication) throws CisException {
        Token token = (Token) authentication;
        return new ResponseEntity<>(namespaceService.createNamespacePermissionsOfUser(namespaceId, token.getUserName(), role, token.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @GetMapping(value = "/schemes/{schemeName}/permissions")
    public ResponseEntity<List<PermissionsScheme>> getPermissionsForScheme(@PathVariable String schemeName) throws CisException {
        return new ResponseEntity<>(schemeService.getPermissionsForScheme(schemeName), HttpStatus.OK);
    }


    @DeleteMapping("/schemes/{schemeName}/permissions/{username}")
    public ResponseEntity<String> deleteSchemePermissions(@PathVariable String schemeName, @PathVariable String username, Authentication authentication) throws CisException {
        Token token = (Token) authentication;
        return new ResponseEntity<>(schemeService.deleteSchemePermissions(schemeName, username, token.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @PostMapping("/schemes/{schemeName}/permissions/{username}")
    public ResponseEntity<String> createSchemePermissions(@PathVariable String schemeName, @RequestParam String role, Authentication authentication) throws CisException {
        Token token = (Token) authentication;
        return new ResponseEntity<>(schemeService.createSchemePermissions(schemeName, role, token.getAuthenticateResponseDto()), HttpStatus.OK);
    }

}
