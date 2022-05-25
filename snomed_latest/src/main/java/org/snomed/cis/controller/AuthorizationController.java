package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import org.snomed.cis.domain.PermissionsNamespace;
import org.snomed.cis.domain.PermissionsScheme;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.NamespaceService;
import org.snomed.cis.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "Authorization", value = "Authorization")
@RestController
public class AuthorizationController {

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    SchemeService schemeService;

    @GetMapping("/users/{username}/groups")
    public List<String> getUserGroups(@PathVariable String username, Authentication authentication) {
        Token token = (Token) authentication;
        Collection<GrantedAuthority> authorities = token.getAuthorities();
        return authorities.stream().map(GrantedAuthority::getAuthority).map(s -> s.split("_")[1]).collect(Collectors.toList());
    }

    @GetMapping(value = "/sct/namespaces/{namespaceId}/permissions")
    @ResponseBody
    public List<PermissionsNamespace> getPermissionForNS(@RequestParam String token, @PathVariable String namespaceId) throws CisException {
        return namespaceService.getPermissionForNS(token, namespaceId);
    }

    @GetMapping(value = "/schemes/{schemeName}/permissions")
    @ResponseBody
    public List<PermissionsScheme> getPermissionForScheme(@RequestParam String token, @PathVariable String schemeName) throws CisException {
        return schemeService.getPermissionForScheme(token, schemeName);
    }

    @DeleteMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    @ResponseBody
    public String deleteNamespacePermissions(@RequestParam String token, @PathVariable String namespaceId, @PathVariable String username) throws CisException {
        return namespaceService.deleteNamespacePermissions(token, namespaceId, username);
    }

    @DeleteMapping("/schemes/{schemeName}/permissions/{username}")
    @ResponseBody
    public String deleteSchemesPermissions(@RequestParam String token, @PathVariable String schemeName, @PathVariable String username) throws CisException {
        return schemeService.deleteSchemesPermissions(token, schemeName, username);
    }

    @PostMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    @ResponseBody
    public String createNamespacePermissions(@RequestParam String token, @PathVariable String namespaceId,
                                             @PathVariable String username, @RequestParam String role) throws CisException {
        return namespaceService.createNamespacePermissions(token, namespaceId,
                username, role);
    }

    @PostMapping("/schemes/{schemeName}/permissions/{username}")
    @ResponseBody
    public String createSchemesPermissions(@RequestParam String token, @PathVariable String schemeName,
                                           @PathVariable String username, @RequestParam String role) throws CisException {
        return schemeService.createSchemesPermissions(token, schemeName,
                username, role);
    }

}
