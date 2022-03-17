package com.snomed.api.repository;
import com.snomed.api.domain.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.snomed.api.domain.Sctid;

import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public interface SctidRepository extends JpaRepository<Sctid, Long> {

    @Query(
            value = "SELECT * FROM sctid u",
            nativeQuery = true)
    List<Sctid> getAllSctidsUsingQL();

    @Query(
            value = "SELECT * FROM sctid u where sctid = ?#{[0]}",
            nativeQuery = true)
    Sctid getSctidsById(String id);

    @Query(
            value = "SELECT * FROM sctid u where u.sctid in (:ids)",
            nativeQuery = true)
    List<Sctid> getSctidsByIds(@Param("ids") String ids);

    @Modifying
    @Transactional
    @Query(value="insert into sctid(sctid,sequence,namespace,partitionId,checkDigit,systemId,status) " +
            "values(:sctid,:sequence,:namespace,:partitionId,:checkDigit,:systemId,:status)",
            nativeQuery = true)
    public void insertWithQuery(
            @Param("sctid") String sctid,
            @Param("sequence") long sequence,
            @Param("namespace") int namespace,
            @Param("partitionId") String partitionId,
            @Param("checkDigit") int checkDigit,
            @Param("systemId") String systemId,
            @Param("status") String status
    );

    //find scidrecord by systemId
    @Query(value="SELECT * FROM sctId s WHERE s.systemId IN (:systemIds) and s.namespace = (:namespace)",nativeQuery=true)
    List<Sctid> findSctidBySystemIds(@Param("systemIds") List<String> systemIds, @Param("namespace")Integer namespace);

}
