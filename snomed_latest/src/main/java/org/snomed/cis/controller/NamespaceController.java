package org.snomed.cis.controller;

import org.snomed.cis.controller.dto.NamespaceDto;
import org.snomed.cis.domain.Namespace;
import org.snomed.cis.exception.CisException;
import org.snomed.cis.service.NamespaceService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@Api(tags = "Namespaces", value = "Namespaces")
@RestController
@RequestMapping(path = "/api")
public class NamespaceController {

    @Autowired
    public NamespaceService namespaceService;


    @GetMapping("/users/{username}/namespaces/")
    public ResponseEntity<List<Namespace>> getNamespacesForUser(@RequestParam String token,@PathVariable String username) throws CisException {
        return ResponseEntity.ok(namespaceService.getNamespacesForUser(token,username));
    }

    @GetMapping("/sct/namespaces")
    public List<NamespaceDto> getNamespaces(@RequestParam String token) throws CisException {
    return namespaceService.getNamespaces(token);
    }

    @PostMapping("/sct/namespaces")
    public ResponseEntity<String> createNamespace(@RequestParam String token, @RequestBody NamespaceDto namespace) throws CisException,ParseException {
        return ResponseEntity.ok(namespaceService.createNamespace(token,namespace));
    }

    @PutMapping("/sct/namespaces")
    public ResponseEntity<String> updateNamespace(@RequestParam String token, @RequestBody NamespaceDto namespace) throws Exception {
        return ResponseEntity.ok(namespaceService.updateNamespace(token,namespace));
    }

    @GetMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<NamespaceDto> getNamespace(@RequestParam String token, @PathVariable String namespaceId) throws CisException {
        return ResponseEntity.ok(namespaceService.getNamespace(token,namespaceId));
    }

    @DeleteMapping("/sct/namespaces/{namespaceId}")
    public ResponseEntity<String> deleteNamespace(@RequestParam String token,@PathVariable String namespaceId) throws CisException {
        return ResponseEntity.ok(namespaceService.deleteNamespace(token,namespaceId));
    }


    @PutMapping("/sct/namespaces/{namespaceId}/partition/{partitionId}")
    public ResponseEntity<String> updatePartitionSequence(@RequestParam String token, @PathVariable String namespaceId,@PathVariable String partitionId, @RequestParam String value ) throws CisException {
        return ResponseEntity.ok(namespaceService.updatePartitionSequence(token,namespaceId,partitionId,value));
    }
}
