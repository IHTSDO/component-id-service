package org.snomed.cis.repository;

import org.snomed.cis.domain.NamespaceCount;
import org.snomed.cis.domain.Sctid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface SctidRepository extends JpaRepository<Sctid, String> {

    List<Sctid> findBySystemIdAndNamespace(String systemId, Integer namespace);

    List<Sctid> findBySctidIn(List<String> ids);

    List<Sctid> findBySystemIdInAndNamespace(List<String> systemIds,Integer namespace);

    @Query(value = "select namespace,count(*) from sctid group by namespace",nativeQuery = true)
    List<NamespaceCount> getNamespaceCount();

}
