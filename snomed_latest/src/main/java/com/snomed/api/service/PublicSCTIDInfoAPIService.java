package com.snomed.api.service;

import com.snomed.api.controller.dto.CheckSctidResponseDTO;
import com.snomed.api.controller.dto.NamespacePublicResponse;
import com.snomed.api.domain.Namespace;
import com.snomed.api.exception.APIException;
import com.snomed.api.helper.SctIdHelper;
import com.snomed.api.repository.NamespaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicSCTIDInfoAPIService {
    @Autowired
    NamespaceRepository namespaceRepository;
    @Autowired
    SctidService sctidService;
    @Autowired
    NamespaceService namespaceService;

public List<Namespace> getNamespaces()
{
    List<Namespace> publicNamespaces = namespaceService.getNamespaces();
    return publicNamespaces;
}

public CheckSctidResponseDTO checkSctid(String sctid) throws APIException {
    return sctidService.checkSctid(sctid);
}
}
