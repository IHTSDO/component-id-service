package com.snomed.api.repository;

import com.snomed.api.controller.dto.NamespacePublicResponse;
import com.snomed.api.domain.Namespace;
import com.snomed.api.domain.Partitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface NamespaceRepository extends JpaRepository<Namespace,Integer> {
    Optional<Namespace> findById(Integer namespaceId);
    public List<Namespace> findByNamespace(Integer namespace);
    List<Namespace> findByNamespaceIn(List<Integer> namespaces);


    @Query(value="Delete from namespace where namespace= (:namespace)",nativeQuery = true)
    public List<Namespace> deleteNamespace(Integer namespace);



}
