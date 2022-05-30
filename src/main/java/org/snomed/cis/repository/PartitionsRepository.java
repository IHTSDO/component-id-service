package org.snomed.cis.repository;

import org.snomed.cis.domain.Partitions;
import org.snomed.cis.domain.PartitionsPk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartitionsRepository extends JpaRepository<Partitions, PartitionsPk> {

    List<Partitions> findByNamespace(Integer namespace);

     @Override
    Optional<Partitions> findById(PartitionsPk partitionsPk);

}