package org.snomed.cis.repository;

import org.snomed.cis.domain.BulkJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkJobRepository extends JpaRepository<BulkJob, Integer> {

}
