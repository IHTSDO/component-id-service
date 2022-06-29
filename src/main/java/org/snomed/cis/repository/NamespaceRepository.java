package org.snomed.cis.repository;

import org.snomed.cis.domain.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NamespaceRepository extends JpaRepository<Namespace, Integer> {

    Optional<Namespace> findById(Integer namespaceId);

    List<Namespace> findByNamespaceIn(List<Integer> namespaces);

}
