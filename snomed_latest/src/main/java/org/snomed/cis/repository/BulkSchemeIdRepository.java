package org.snomed.cis.repository;

import org.snomed.cis.domain.SchemeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BulkSchemeIdRepository extends JpaRepository<SchemeId, Integer> {

    public List<SchemeId> findBySchemeAndSchemeIdIn(String scheme, List<String> schemeId);

    public Optional<SchemeId> findBySchemeAndSchemeId(String scheme, String schemeId);


    @Modifying
    @Transactional
    @Query(value = "insert into schemeid (scheme,schemeId, sequence,checkDigit,systemId,status,author,software,expirationDate,jobId,created_at,modified_at)"+
            "values(:scheme,:schemeId, :sequence,:checkDigit,:systemId,:status,:author,:software,:expirationDate,:jobId,:created_at,:modified_at)",
            nativeQuery = true)
    public void insertWithQuery(
            @Param("scheme") String scheme,
            @Param("schemeId") String schemeId,
            @Param("sequence") Integer sequence,
            @Param("checkDigit") Integer checkDigit,
            @Param("systemId") String systemId,
            @Param("status") String status,
            @Param("author") String author,
            @Param("software") String software,
            @Param("expirationDate") LocalDateTime expirationDate,
            @Param("jobId") Integer jobId,
            @Param("created_at") LocalDateTime created_at,
            @Param("modified_at") LocalDateTime modified_at);

    @Query(value="Select * from schemeid order by schemeId",nativeQuery = true)
    public List<SchemeId> findBySchemeid();

    List<SchemeId> findBySchemeAndSystemId(String schemeName, String systemid);
}
