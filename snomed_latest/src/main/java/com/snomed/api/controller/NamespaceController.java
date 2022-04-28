package com.snomed.api.controller;

import com.snomed.api.controller.dto.NamespaceDto;
import com.snomed.api.controller.dto.NamespacePublicResponse;
import com.snomed.api.domain.Namespace;
import com.snomed.api.exception.APIException;
import com.snomed.api.repository.NamespaceRepository;
import com.snomed.api.service.NamespaceService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Namespaces", value = "Namespaces")
@RestController
public class NamespaceController {

    @Autowired
    public NamespaceService namespaceService;


    @GetMapping("/users/{username}/namespaces")
    public ResponseEntity<List<Namespace>> getNamespacesForUser(@RequestParam String token,@PathVariable String username) throws APIException {
        return ResponseEntity.ok(namespaceService.getNamespacesForUser(token,username));
    }

    @GetMapping("/sct/namespaces")
    public ResponseEntity<List<Namespace>> getNamespaces(@RequestParam String token) throws APIException {
    return ResponseEntity.ok(namespaceService.getNamespaces(token));
    }

    @PostMapping("/sct/namespaces")
    public ResponseEntity<String> createNamespace(@RequestParam String token, @RequestBody Namespace namespace) throws APIException {
        return ResponseEntity.ok(namespaceService.createNamespace(token,namespace));
    }

    @PutMapping("/sct/namespaces")
    public ResponseEntity<Namespace> updateNamespace(@RequestParam String token, @RequestBody Namespace namespace) throws APIException {
        return ResponseEntity.ok(namespaceService.updateNamespace(token,namespace));
    }

    @GetMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<NamespaceDto> getNamespace(@RequestParam String token, @PathVariable String namespaceId) throws APIException {
        return ResponseEntity.ok(namespaceService.getNamespace(token,namespaceId));
    }

    @DeleteMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<String> deleteNamespace(@RequestParam String token,@PathVariable String namespaceId) throws APIException {
        return ResponseEntity.ok(namespaceService.deleteNamespace(token,namespaceId));
    }


    @PutMapping("/sct/namespaces/{namespaceId}/partition/{partitionId}")
    public ResponseEntity<Namespace> updatePartitionSequence(@RequestParam String token, @PathVariable String namespaceId,@PathVariable String partitionId, @RequestParam String value ) throws APIException {
        return ResponseEntity.ok(namespaceService.updatePartitionSequence(token,namespaceId,partitionId,value));
    }
}
