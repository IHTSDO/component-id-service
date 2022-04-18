package com.snomed.api.controller;

import com.snomed.api.controller.dto.NamespacePublicResponse;
import com.snomed.api.domain.Namespace;
import com.snomed.api.exception.APIException;
import com.snomed.api.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NamespaceController {

    @Autowired
    public NamespaceService namespaceService;


    @GetMapping("/users/{username}/namespaces")
    public ResponseEntity<List<Namespace>> getNamespacesForUser(@PathVariable String userName) throws APIException {
        return ResponseEntity.ok(namespaceService.getNamespacesForUser(userName));
    }

    @GetMapping("/sct/namespaces")
    public ResponseEntity<List<Namespace>> getNamespaces(@RequestParam String token){
    return ResponseEntity.ok(namespaceService.getNamespaces());
    }

    /*@PostMapping("sct/namespaces")
    public ResponseEntity<List<Namespace>> createNamespace(*//*String token,*//* @RequestBody Namespace namespace) throws APIException {
        return ResponseEntity.ok(namespaceService.createNamespace(namespace));
    }*/

    @PutMapping("sct/namespaces")
    public ResponseEntity<Namespace> updateNamespace(/*String token,*/ @RequestBody Namespace namespace) throws APIException {
        return ResponseEntity.ok(namespaceService.updateNamespace(namespace));
    }

}
