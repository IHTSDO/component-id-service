package org.snomed.cis.repository;

import org.snomed.cis.domain.PermissionsNamespace;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface PermissionsNamespaceRepository extends JpaRepository<PermissionsNamespace, Integer> {

    List<PermissionsNamespace> findByNamespace(Integer namespace);

    List<PermissionsNamespace> findByUsernameIn(List<String> usernames);

    long deleteByNamespaceAndUsername(Integer namespace, String username);

    Optional<PermissionsNamespace> findByNamespaceAndUsernameAndRole(Integer namespace, String username, String role);

}
