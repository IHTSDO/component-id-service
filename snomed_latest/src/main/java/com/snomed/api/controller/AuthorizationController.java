package com.snomed.api.controller;

import com.snomed.api.domain.PermissionsNamespace;
import com.snomed.api.domain.PermissionsScheme;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.NamespaceService;
import com.snomed.api.service.SchemeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Authorization", value = "Authorization")
@RestController
@RequestMapping(path = "/api")
public class AuthorizationController {

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    SchemeService schemeService;

    @GetMapping(value="/sct/namespaces/{namespaceId}/permissions")
    @ResponseBody
    public List<PermissionsNamespace> getPermissionForNS(@RequestParam String token, @PathVariable String namespaceId) throws APIException {
        return namespaceService.getPermissionForNS(token,namespaceId);
    }

    @GetMapping(value="/schemes/{schemeName}/permissions")
    @ResponseBody
    public List<PermissionsScheme> getPermissionForScheme(@RequestParam String token, @PathVariable String schemeName) throws APIException {
        return schemeService.getPermissionForScheme(token,schemeName);
    }

    @DeleteMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    @ResponseBody
    public String deleteNamespacePermissions(@RequestParam String token, @PathVariable String namespaceId, @PathVariable String username) throws APIException {
        return namespaceService.deleteNamespacePermissions(token,namespaceId,username);
    }

    @DeleteMapping("/schemes/{schemeName}/permissions/{username}")
    @ResponseBody
    public String deleteSchemesPermissions(@RequestParam String token, @PathVariable String schemeName, @PathVariable String username) throws APIException {
        return schemeService.deleteSchemesPermissions(token,schemeName,username);
    }

    @PostMapping("/sct/namespaces/{namespaceId}/permissions/{username}")
    @ResponseBody
    public String createNamespacePermissions(@RequestParam String token, @PathVariable String namespaceId,
                                             @PathVariable String username, @RequestParam String role) throws APIException {
        return namespaceService.createNamespacePermissions(token, namespaceId,
               username, role);
    }

    @PostMapping("/schemes/{schemeName}/permissions/{username}")
    @ResponseBody
    public String createSchemesPermissions(@RequestParam String token, @PathVariable String schemeName,
                                             @PathVariable String username, @RequestParam String role) throws APIException {
        return schemeService.createSchemesPermissions(token, schemeName,
                username, role);
    }

}
