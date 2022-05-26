package org.snomed.cis.controller;

import io.swagger.annotations.Api;
import org.snomed.cis.controller.dto.NamespaceDto;
import org.snomed.cis.domain.Namespace;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@Api(tags = "Namespaces", value = "Namespaces")
@RestController
public class NamespaceController {

    @Autowired
    public NamespaceService namespaceService;


    @GetMapping("/users/{username}/namespaces/")
    public ResponseEntity<List<Namespace>> getNamespacesForUser(@RequestParam String token,@PathVariable String username) throws CisException {
        return ResponseEntity.ok(namespaceService.getNamespacesForUser(token,username));
    }

    @GetMapping("/sct/namespaces")
    @ResponseBody
    public List<NamespaceDto> getNamespaces(@RequestParam String token) throws CisException {
    return namespaceService.getNamespaces(token);
    }

    @PostMapping("/sct/namespaces")
    public ResponseEntity<String> createNamespace(@RequestParam String token, @RequestBody NamespaceDto namespace,Authentication authentication) throws CisException,ParseException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(namespaceService.createNamespace(authToken.getAuthenticateResponseDto(),namespace));
    }

    @PutMapping("/sct/namespaces")
    public ResponseEntity<String> updateNamespace(@RequestParam String token, @RequestBody NamespaceDto namespace, Authentication authentication) throws Exception {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(namespaceService.updateNamespace(authToken.getAuthenticateResponseDto(),namespace));
    }

    @GetMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<NamespaceDto> getNamespace(@RequestParam String token, @PathVariable String namespaceId) throws CisException {
        return ResponseEntity.ok(namespaceService.getNamespace(token,namespaceId));
    }

    @DeleteMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<String> deleteNamespace(@RequestParam String token,@PathVariable String namespaceId,Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(namespaceService.deleteNamespace(authToken.getAuthenticateResponseDto(),namespaceId));
    }


    @PutMapping("/sct/namespaces/{namespaceId}/partition/{partitionId}")
    public ResponseEntity<String> updatePartitionSequence(@RequestParam String token, @PathVariable String namespaceId,@PathVariable String partitionId, @RequestParam String value,Authentication authentication ) throws CisException {
        Token authToken = (Token) authentication;
        return ResponseEntity.ok(namespaceService.updatePartitionSequence(authToken.getAuthenticateResponseDto(),namespaceId,partitionId,value));
    }
}
