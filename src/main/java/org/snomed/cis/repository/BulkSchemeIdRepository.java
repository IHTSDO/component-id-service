package org.snomed.cis.repository;

import org.snomed.cis.domain.SchemeId;
import org.snomed.cis.domain.SchemeIdKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface BulkSchemeIdRepository extends JpaRepository<SchemeId, SchemeIdKey> {

    List<SchemeId> findBySchemeAndSchemeIdIn(String scheme, List<String> schemeId);

    Optional<SchemeId> findBySchemeAndSchemeId(String scheme, String schemeId);

    List<SchemeId> findBySchemeAndSystemId(String schemeName, String systemid);

//    List<SchemeId> findBySchemeAndSystemIdIn(String schemeName, List<String> systemid);

    @Query(value="Select * from schemeid order by schemeId",nativeQuery = true)
    List<SchemeId> findBySchemeid();
// CIS-52
    List<SchemeId> findBySchemeAndSystemIdIn(String schemeName, List<String> systemIds);

    @Transactional
    @Modifying
    @Query(value = "UPDATE schemeid SET JobId=(:jobId), modified_at=now() WHERE systemId in (:systemIds) and scheme=(:scheme)",nativeQuery = true)
    int update(@Param("systemIds") List<String> systemIds, @Param("scheme") String scheme, @Param("jobId") Integer jobId);

}
