package org.snomed.cis.repository;

import org.snomed.cis.domain.BulkJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Optional;

public interface BulkJobRepository extends JpaRepository<BulkJob, Integer> {
    @Transactional
    @Modifying
    @Query(value="update bulkjob set status = (:status), log = (:log) where id = (:id)",nativeQuery = true)
    int updateBulkJobStatus(@Param("status") String status,@Param("log") String log, @Param("id") Integer id);

    @Transactional
    @Modifying
    @Query(value="update bulkjob set status = (:status), request=(:request), log = (:log) where id = (:id)",nativeQuery = true)
    int updateBulkJobStatusWithReq(@Param("status") String status,@Param("request") String request,@Param("log") String log, @Param("id") Integer id);

    @Query(value="select * from bulkjob where status=(:status) order by created_at limit 1",nativeQuery = true)
    Optional<BulkJob> findTopByStatusOrderByCreated_at(@Param("status") String status);
}
