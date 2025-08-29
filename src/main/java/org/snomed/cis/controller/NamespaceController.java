package org.snomed.cis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.cis.domain.Namespace;
import org.snomed.cis.domain.Partitions;
import org.snomed.cis.dto.GetNameSpaceDTO;
import org.snomed.cis.dto.NameSpaceResponseDTO;
import org.snomed.cis.dto.NamespaceDto;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.security.Token;
import org.snomed.cis.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;


import java.util.Collections;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "Namespaces" , description = "Namespaces Controller")
@RestController
public class NamespaceController {
    private final Logger logger = LoggerFactory.getLogger(NamespaceController.class);
    @Autowired
    public NamespaceService namespaceService;

    private static final String NO_NAMESPACES_FOUND_MESSAGE = "No namespaces found";

    @Operation(summary = "getNamespacesForUser")
    @GetMapping("/users/{username}/namespaces/")
    public ResponseEntity<List<NameSpaceResponseDTO>> getNamespacesForUser(@RequestParam String token, @PathVariable String username) throws CisException {
        logger.info("Request received for - username :: {}", username);

        List<Namespace> namespaces = namespaceService.getNamespacesForUser(username);
        if (namespaces == null || namespaces.isEmpty()) {
            throw new CisException( HttpStatus.NOT_FOUND,NO_NAMESPACES_FOUND_MESSAGE);
        }

        List<NameSpaceResponseDTO> response = namespaces.stream()
                .map(ns -> {
                    NameSpaceResponseDTO dto = new NameSpaceResponseDTO();
                    dto.setNamespace(ns.getNamespace());
                    dto.setOrganizationName(ns.getOrganizationName());
                    dto.setDateIssued(ns.getDateIssued());
                    dto.setNotes(ns.getNotes());
                    dto.setIdPregenerate(ns.getIdPregenerate());
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "getNamespaces")
    @GetMapping("/sct/namespaces")
    public ResponseEntity<List<GetNameSpaceDTO>> getNamespaces(@RequestParam(required = false) String token) throws CisException {
        logger.info("Request received for - No ReqParam");

        List<NamespaceDto> originalList = namespaceService.getNamespaces();
        if (originalList == null || originalList.isEmpty()) {
            throw new CisException(HttpStatus.NOT_FOUND,NO_NAMESPACES_FOUND_MESSAGE);
        }

        List<GetNameSpaceDTO> response = originalList.stream()
                .map(this::mapToGetNamespaceDTO)
                .filter(Objects::nonNull)
                .toList();

        if (response.isEmpty()) {
            throw new CisException( HttpStatus.NOT_FOUND,NO_NAMESPACES_FOUND_MESSAGE);
        }


        return ResponseEntity.ok(response);
    }

    @Operation(summary = "getNamespace")
    @GetMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<GetNameSpaceDTO> getNamespace(@RequestParam String token, @PathVariable String namespaceId) throws CisException {
        logger.info("Request received for - namespaceId :: {}", namespaceId);

        NamespaceDto ns = namespaceService.getNamespace(namespaceId);
        if (ns == null) {
            throw new CisException(HttpStatus.NOT_FOUND,"Namespace with id " + namespaceId + " not found");
        }
        GetNameSpaceDTO dto = mapToGetNamespaceDTO(ns);

        return ResponseEntity.ok(dto);
    }

    private GetNameSpaceDTO mapToGetNamespaceDTO(NamespaceDto ns) {
        if (ns == null) {
            return null;
        }
        GetNameSpaceDTO dto = new GetNameSpaceDTO();
        dto.setNamespace(ns.getNamespace());
        dto.setOrganizationName(ns.getOrganizationName());
        dto.setDateIssued(ns.getDateIssued());
        dto.setNotes(ns.getNotes());
        dto.setIdPregenerate(ns.getIdPregenerate());
        List<Partitions> partList = Optional.ofNullable(ns.getPartitions())
                .orElse(Collections.emptyList())
                .stream()
                .filter(partition -> partition.getNamespace().equals(ns.getNamespace()) )
                .toList();
        dto.setPartitions(partList);
        return dto;
    }

    @Operation(summary = "createNamespace")
    @PostMapping("/sct/namespaces")
    public ResponseEntity<String> createNamespace(@RequestParam String token, @RequestBody NamespaceDto namespace,@Parameter(hidden = true) Authentication authentication) throws CisException {
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
