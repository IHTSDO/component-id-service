package org.snomed.cis.repository;

import org.snomed.cis.controller.dto.QueryCountByNamespaceDto;
import org.snomed.cis.domain.Sctid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SctidRepository extends JpaRepository<Sctid, String> {

    List<Sctid> findBySystemIdAndNamespace(String systemId, Integer namespace);

    List<Sctid> findBySctidIn(List<String> ids);

    List<Sctid> findBySystemIdInAndNamespace(List<String> systemIds, Integer namespace);

    @Query(value = "select namespace,count(*) as count from sctid group by namespace", nativeQuery = true)
    List<QueryCountByNamespaceDto> getCountByNamespace();

    @Query(value = "select namespace,count(*) as count from sctid group by namespace having namespace IN :namespaces", nativeQuery = true)
    List<QueryCountByNamespaceDto> getCountByNamespace(List<String> namespaces);

}
