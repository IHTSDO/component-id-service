package org.snomed.cis.repository;

import org.snomed.cis.domain.PermissionsNamespace;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface PermissionsNamespaceRepository extends JpaRepository<PermissionsNamespace,Integer> {

 List<PermissionsNamespace> findByNamespace(Integer namespace);

 List<PermissionsNamespace> findByUsernameIn(List<String> user);

 long deleteByNamespaceAndUsername(Integer namespace, String username);

}
