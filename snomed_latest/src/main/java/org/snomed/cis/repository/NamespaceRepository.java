package org.snomed.cis.repository;

import org.snomed.cis.domain.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NamespaceRepository extends JpaRepository<Namespace,Integer> {
    Optional<Namespace> findById(Integer namespaceId);
    public List<Namespace> findByNamespace(Integer namespace);
    List<Namespace> findByNamespaceIn(List<Integer> namespaces);


    @Query(value="Delete from namespace where namespace= (:namespace)",nativeQuery = true)
    public List<Namespace> deleteNamespace(Integer namespace);

    /*@Query(value="select p from namespace n join partitions p on n.namespace = p.namespace",nativeQuery = true)
public  List<NamespaceDto> getAllNamespace();*/

}
