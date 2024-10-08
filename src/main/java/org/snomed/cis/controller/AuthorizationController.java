package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@Tag(name = "Authorization" , description = "Authorization Controller")
@RestController
public class AuthorizationController {
    private final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);
    @Autowired
    NamespaceService namespaceService;

    @Autowired
    SchemeService schemeService;

    @Autowired
    private AuthorizationService authorizationService;


    @Operation(summary = "getUsers")
    @GetMapping("/users")
    public ResponseEntity<List<String>> getUsers(@RequestParam String token, @RequestParam(required = false) String searchString) throws CisException {
        return new ResponseEntity<>(authorizationService.getUsers(searchString), HttpStatus.OK);
    }

    @Operation(summary = "getUserGroups")
    @GetMapping("/users/{username}/groups")
    public ResponseEntity<List<String>> getUserGroups(@RequestParam String token, @Parameter(hidden = true) Authentication authentication, @PathVariable String username) throws CisException {
        return new ResponseEntity<>(authorizationService.getUserGroups(username), HttpStatus.OK);
    }

    @Operation(summary = "removeMember")
    @DeleteMapping("/users/{username}/groups/{groupName}")
    public ResponseEntity<Void> removeMember(@RequestParam String token, @PathVariable String username, @PathVariable String groupName, @Parameter(hidden = true) Authentication authentication) throws CisException {
        authorizationService.removeMember(username, groupName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "addMember")
    @PostMapping("/users/{username}/groups/{groupName}")
    public ResponseEntity<Void> addMember(@RequestParam String token, @PathVariable String username, @PathVariable String groupName, @Parameter(hidden = true) Authentication authentication) throws CisException {
        authorizationService.addMember(username, groupName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "getGroups")
    @GetMapping("/groups")
    public ResponseEntity<List<String>> getGroups(@RequestParam String token) throws CisException {
        return new ResponseEntity<>(authorizationService.getGroups(), HttpStatus.OK);
    }

    @Operation(summary = "getGroupUsers")
    @GetMapping("/groups/{groupName}/users")
    public ResponseEntity<List<String>> getGroupUsers(@RequestParam String token, @PathVariable String groupName) throws CisException {
        return new ResponseEntity<>(authorizationService.getGroupUsers(groupName), HttpStatus.OK);
    }

    @Operation(summary = "getNamespacePermissions")
    @GetMapping(value = "/sct/namespaces/{namespaceId}/permissions")
    public ResponseEntity<List<PermissionsNamespace>> getNamespacePermissions(@RequestParam String token, @PathVariable String namespaceId) throws CisException {
        return new ResponseEntity<>(namespaceService.getNamespacePermissions(namespaceId), HttpStatus.OK);
    }

    @Operation(summary = "deleteNamespacePermissionsOfUser")
    @DeleteMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    public ResponseEntity<String> deleteNamespacePermissionsOfUser(@RequestParam String token, @PathVariable String namespaceId, @PathVariable String username, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(namespaceService.deleteNamespacePermissionsOfUser(namespaceId, username, authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @Operation(summary = "createNamespacePermissionsOfUser")
    @PostMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    public ResponseEntity<String> createNamespacePermissionsOfUser(@RequestParam String token, @PathVariable String namespaceId, @PathVariable String username, @RequestParam String role, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(namespaceService.createNamespacePermissionsOfUser(namespaceId, username, role, authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @Operation(summary = "getPermissionsForScheme")
    @GetMapping(value = "/schemes/{schemeName}/permissions")
    public ResponseEntity<List<PermissionsScheme>> getPermissionsForScheme(@RequestParam String token, @PathVariable String schemeName) throws CisException {
        return new ResponseEntity<>(schemeService.getPermissionsForScheme(schemeName), HttpStatus.OK);
    }

    @Operation(summary = "deleteSchemePermissions")
    @DeleteMapping("/schemes/{schemeName}/permissions/{username}")
    public ResponseEntity<String> deleteSchemePermissions(@RequestParam String token, @PathVariable String schemeName, @PathVariable String username, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(schemeService.deleteSchemePermissions(schemeName, username, authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

    @Operation(summary = "createSchemePermissions")
    @PostMapping("/schemes/{schemeName}/permissions/{username}")
    public ResponseEntity<String> createSchemePermissions(@RequestParam String token, @PathVariable String schemeName, @PathVariable String username, @RequestParam String role, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return new ResponseEntity<>(schemeService.createSchemePermissions(schemeName, username, role, authToken.getAuthenticateResponseDto()), HttpStatus.OK);
    }

}
