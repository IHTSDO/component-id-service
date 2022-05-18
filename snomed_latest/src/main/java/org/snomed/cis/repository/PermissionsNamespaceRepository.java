package org.snomed.cis.repository;

import org.snomed.cis.domain.PermissionsNamespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
@Repository
@Transactional
public interface PermissionsNamespaceRepository extends JpaRepository<PermissionsNamespace,Integer> {

 //@Query(value = "select * from permissionsnamespace where namespace=(:namespace)",nativeQuery = true)
 List<PermissionsNamespace> findByNamespace(Integer namespace);

 List<PermissionsNamespace> findByUsername(String username);

 List<PermissionsNamespace> findByUsernameIn(List<String> user);

 long deleteByNamespaceAndUsername(Integer namespace, String username);

}
