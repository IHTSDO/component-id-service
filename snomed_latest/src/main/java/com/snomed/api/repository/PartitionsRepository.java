package com.snomed.api.repository;

import com.snomed.api.domain.Namespace;
import com.snomed.api.domain.Partitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartitionsRepository extends JpaRepository<Partitions, Namespace> {
    List<Partitions> findByNamespaceAndPartitionId(Integer namespace, String partitionId);
}
