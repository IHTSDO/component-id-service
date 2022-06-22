package org.snomed.cis.repository;

import org.snomed.cis.domain.BulkJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface BulkJobRepository extends JpaRepository<BulkJob, Integer> {
    @Transactional
    @Modifying
    @Query(value="update bulkjob set status = (:status), log = (:log) where id = (:id)",nativeQuery = true)
    int updateBulkJobStatus(@Param("status") String status,@Param("log") String log, @Param("id") Integer id);

}
