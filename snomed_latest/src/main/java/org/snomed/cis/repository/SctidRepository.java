package org.snomed.cis.repository;

import org.snomed.cis.domain.Sctid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SctidRepository extends JpaRepository<Sctid, String> {

    List<Sctid> findBySystemIdAndNamespace(String systemId, Integer namespace);

    List<Sctid> findBySctidIn(List<String> ids);

    List<Sctid> findBySystemIdInAndNamespace(Collection<String> systemIds,Integer namespace);

}
