package com.snomed.api.repository;

import com.snomed.api.controller.dto.NamespacePublicResponse;
import com.snomed.api.domain.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface NamespaceRepository extends JpaRepository<Namespace,Integer> {
    Optional<Namespace> findById(Integer namespaceId);
    public List<Namespace> findByNamespace(Integer namespace);
    //@Query(value="Select namespace,organizationName,organizationAndContactDetails,dateIssued,email,notes  from namespace ",nativeQuery = true)
    //public List<NamespacePublicResponse> findAllNamespace();
}
