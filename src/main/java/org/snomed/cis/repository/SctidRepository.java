package org.snomed.cis.repository;

import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.QueryCountByNamespaceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface SctidRepository extends JpaRepository<Sctid, String> {

    List<Sctid> findBySystemIdAndNamespace(String systemId, Integer namespace);

    List<Sctid> findBySctidIn(List<String> ids);

    List<Sctid> findBySystemIdInAndNamespace(List<String> systemIds, Integer namespace);

    @Query(value = "select namespace,count(*) as count from sctid group by namespace", nativeQuery = true)
    List<QueryCountByNamespaceDto> getCountByNamespace();

    @Query(value = "select namespace,count(*) as count from sctid group by namespace having namespace IN :namespaces", nativeQuery = true)
    List<QueryCountByNamespaceDto> getCountByNamespace(List<String> namespaces);

    Sctid findBySctidAndSystemId(int sctId, String systemId);

    @Query(value = "UPDATE sctId SET JobId=(:jobId) modified_at=:now() WHERE systemId in (:systemIds)", nativeQuery = true)
    List<Sctid> update(@Param("systemIds") List<Sctid> systemIds, Integer jobId);

    @Query(value = "UPDATE sctId SET JobId=(:jobId) modified_at=:now() WHERE sctid in (:sctids)", nativeQuery = true)
    List<Sctid> updateSctid(@Param("sctids") List<Sctid> sctids, Integer jobId);


    List<Sctid> findByNamespaceAndPartitionId(Integer namespace, String partitionId);
}
