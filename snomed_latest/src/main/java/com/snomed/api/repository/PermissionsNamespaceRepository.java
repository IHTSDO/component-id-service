package com.snomed.api.repository;

import com.snomed.api.domain.Namespace;
import com.snomed.api.domain.PermissionsNamespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
@Repository
@Transactional
public interface PermissionsNamespaceRepository extends JpaRepository<PermissionsNamespace,Long> {

 List<PermissionsNamespace> findByNamespace(Integer namespace);

 List<PermissionsNamespace> findByUsername(String username);

 List<PermissionsNamespace> findByUsernameIn(List<String> user);

}
