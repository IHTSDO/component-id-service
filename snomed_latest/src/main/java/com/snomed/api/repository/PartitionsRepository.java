package com.snomed.api.repository;

import com.snomed.api.domain.Partitions;
import com.snomed.api.domain.PartitionsPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartitionsRepository extends JpaRepository<Partitions, PartitionsPk> {
    @Query(value = "Select * from partitions where namespace=(:namespace)", nativeQuery = true)
    public List<Partitions> findByNamespace(Integer namespace);


    /*@Query(value = "Delete from partitions where namespace= (:namespace) and partitionId=(:partitionId)", nativeQuery = true)
    public int deleteNamespace(Integer namespace,String partitionId);*/

     @Override
    Optional<Partitions> findById(PartitionsPk partitionsPk);

}