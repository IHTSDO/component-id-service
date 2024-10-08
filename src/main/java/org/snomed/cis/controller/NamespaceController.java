package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.Namespace;
import org.snomed.cis.dto.NamespaceDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import java.text.ParseException;
import java.util.List;

@Tag(name = "Namespaces" , description = "Namespaces Controller")
@RestController
public class NamespaceController {
    private final Logger logger = LoggerFactory.getLogger(NamespaceController.class);
    @Autowired
    public NamespaceService namespaceService;


    @Operation(summary = "getNamespacesForUser")
    @GetMapping("/users/{username}/namespaces/")
    public ResponseEntity<List<Namespace>> getNamespacesForUser(@RequestParam String token, @PathVariable String username) throws CisException {
        logger.info("Request received for - username :: {}", username);
        return ResponseEntity.ok(namespaceService.getNamespacesForUser(username));
    }

    @Operation(summary = "getNamespaces")
    @GetMapping("/sct/namespaces")
    public ResponseEntity<List<NamespaceDto>> getNamespaces(@RequestParam(required = false) String token) throws CisException {
        logger.info("Request received for - No ReqParam");
        return ResponseEntity.ok(namespaceService.getNamespaces());
    }

    @Operation(summary = "createNamespace")
    @PostMapping("/sct/namespaces")
    public ResponseEntity<String> createNamespace(@RequestParam String token, @RequestBody NamespaceDto namespace,@Parameter(hidden = true) Authentication authentication) throws CisException,ParseException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - NamespaceDto :: {} - authenticateResponseDto :: {}", namespace,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(namespaceService.createNamespace(authToken.getAuthenticateResponseDto(),namespace));
    }

    @Operation(summary = "updateNamespace")
    @PutMapping("/sct/namespaces")
    public ResponseEntity<String> updateNamespace(@RequestParam String token, @RequestBody NamespaceDto namespace, @Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - NamespaceDto :: {} - authenticateResponseDto :: {}", namespace,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(namespaceService.updateNamespace(authToken.getAuthenticateResponseDto(),namespace));
    }

    @Operation(summary = "getNamespace")
    @GetMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<NamespaceDto> getNamespace(@RequestParam String token, @PathVariable String namespaceId) throws CisException {
        logger.info("Request received for - namespaceId :: {}", namespaceId);
        return ResponseEntity.ok(namespaceService.getNamespace(namespaceId));
    }

    @Operation(summary = "deleteNamespace")
    @DeleteMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<String> deleteNamespace(@RequestParam String token, @PathVariable String namespaceId,@Parameter(hidden = true) Authentication authentication) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - NamespaceDto :: {} - authenticateResponseDto :: {}", namespaceId,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(namespaceService.deleteNamespace(authToken.getAuthenticateResponseDto(),namespaceId));
    }


    @Operation(summary = "updatePartitionSequence")
    @PutMapping("/sct/namespaces/{namespaceId}/partition/{partitionId}")
    public ResponseEntity<String> updatePartitionSequence(@RequestParam String token, @PathVariable String namespaceId,@PathVariable String partitionId, @RequestParam String value,@Parameter(hidden = true) Authentication authentication ) throws CisException {
        Token authToken = (Token) authentication;
        logger.info("Request received for - namespaceId :: {} - partitionId :: {} - value :: {} - authenticateResponseDto :: {}", namespaceId,partitionId,value,authToken.getAuthenticateResponseDto().toString());
        return ResponseEntity.ok(namespaceService.updatePartitionSequence(authToken.getAuthenticateResponseDto(),namespaceId,partitionId,value));
    }
}
