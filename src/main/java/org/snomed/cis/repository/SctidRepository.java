package org.snomed.cis.repository;

import org.snomed.cis.domain.Sctid;
import org.snomed.cis.dto.QueryCountByNamespaceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.util.List;

public interface SctidRepository extends JpaRepository<Sctid, String> {

    List<Sctid> findBySystemIdAndNamespace(String systemId, Integer namespace);

    List<Sctid> findBySctidIn(List<String> ids);

    List<Sctid> findBySystemIdInAndNamespace(List<String> systemIds, Integer namespace);

    @Query(value="SELECT systemId FROM sctId WHERE systemId in (:systemIds) and namespace=:namespace",nativeQuery = true)
    List<String> getSystemIdByNamespace(@Param("systemIds") List<String> systemIds,@Param("namespace") Integer namespace);

    @Query(value = "select namespace,count(*) as count from sctid group by namespace", nativeQuery = true)
    List<QueryCountByNamespaceDto> getCountByNamespace();

    @Query(value = "select namespace,count(*) as count from sctid group by namespace having namespace IN :namespaces", nativeQuery = true)
    List<QueryCountByNamespaceDto> getCountByNamespace(List<String> namespaces);


@Transactional
@Modifying
    @Query(value = "UPDATE sctid SET jobId= ?1,modified_at=now() WHERE systemId in (?2)", nativeQuery = true)
    int updateJobIdInSctid(Integer jobId,List<String> systemIds);
@Transactional
@Modifying
    @Query(value = "UPDATE sctid SET jobId=(:jobId),modified_at=now(),status=(case when status in('Available','Reserved') then 'Assigned' else status end) WHERE sctid in (:sctids)", nativeQuery = true)
    int updateSctid(@Param("sctids") List<String> sctids, @Param("jobId")Integer jobId);

    List<Sctid> findByNamespaceAndPartitionId(Integer namespace, String partitionId);
}
